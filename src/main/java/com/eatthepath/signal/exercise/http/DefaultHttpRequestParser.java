package com.eatthepath.signal.exercise.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DefaultHttpRequestParser implements HttpRequestParser {

    private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^([a-zA-Z]+) ([^\\s]+) (HTTP/.+)$");

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpRequestParser.class);

    @Override
    public HttpRequest parseHttpRequest(final ByteBuffer buffer) throws IncompleteHttpRequestException, InvalidHttpRequestException {
        // This is memory-inefficient and GC-heavy, but it's pretty quick to implement. Given a 4-6 hour window to put
        // together an HTTP server, this is a case where I'll flag that there's lots of room for improvement and plan
        // to revisit it later.
        final byte[] requestBytes = new byte[buffer.limit()];
        buffer.get(requestBytes);

        final String requestString = new String(requestBytes);

        // We need at least one line (the request line). Bail our early if we haven't made it that far.
        if (!requestString.contains("\r\n")) {
            throw new IncompleteHttpRequestException();
        }

        try (final BufferedReader reader = new BufferedReader(new StringReader(requestString))) {
            final HttpRequestMethod requestMethod;
            final String path;
            final String httpVersion;
            {
                final String requestLine = reader.readLine();

                if (requestLine == null) {
                    throw new IncompleteHttpRequestException();
                }

                final Matcher requestLineMatcher = REQUEST_LINE_PATTERN.matcher(requestLine);

                if (requestLineMatcher.matches()) {
                    requestMethod =
                            HttpRequestMethod.valueOf(requestLineMatcher.group(1).toUpperCase().trim());

                    path = requestLineMatcher.group(2);

                    httpVersion = requestLineMatcher.group(3);
                } else {
                    // We know we have a complete request line because we checked for a CRLF earlier; that means that
                    // if the pattern doesn't match, the request line is bogus and won't be remedied by getting more
                    // data.
                    throw new InvalidHttpRequestException("Invalid request line");
                }
            }

            final Map<String, String> headers = new HashMap<>();
            {
                String line = reader.readLine();
                boolean foundEmptyLine = false;

                while (line != null) {
                    if (line.length() == 0) {
                        foundEmptyLine = true;
                        break;
                    } else {
                        final String[] headerPieces = line.split(":", 2);

                        if (headerPieces.length != 2) {
                            throw new InvalidHttpRequestException("Malformed header line: " + line);
                        }

                        headers.put(headerPieces[0], headerPieces[1].trim());

                        line = reader.readLine();
                    }
                }

                if (!foundEmptyLine) {
                    // The data available to us ends before the end of the header block
                    throw new IncompleteHttpRequestException();
                }
            }

            final String requestBody;

            if (requestMethod == HttpRequestMethod.POST) {
                // We need to read/parse the request body
                if (!headers.containsKey("Content-Length")) {
                    throw new InvalidHttpRequestException("No Content-Length specified for request with expected response body");
                }

                final int contentLength;

                try {
                    contentLength = Integer.parseInt(headers.get("Content-Length"), 10);
                } catch (final NumberFormatException e) {
                    throw new InvalidHttpRequestException(e);
                }

                final char[] bodyChars = new char[contentLength];

                final int charsRead = reader.read(bodyChars);
                requestBody = new String(bodyChars, 0, charsRead);

                if (requestBody.getBytes().length != contentLength) {
                    throw new IncompleteHttpRequestException();
                }
            } else {
                requestBody = null;
            }

            return new DefaultHttpRequest(requestMethod, path, httpVersion, headers, requestBody);
        } catch (final IOException e) {
            // This should never happen for string readers
            log.error("Exception while reading request string", e);
        }

        throw new InvalidHttpRequestException("Unexpected error processing HTTP request");
    }
}
