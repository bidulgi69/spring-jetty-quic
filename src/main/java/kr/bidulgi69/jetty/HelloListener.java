package kr.bidulgi69.jetty;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.http3.api.Session;
import org.eclipse.jetty.http3.api.Stream;
import org.eclipse.jetty.http3.frames.DataFrame;
import org.eclipse.jetty.http3.frames.HeadersFrame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HelloListener implements Session.Server.Listener {

    @Override
    public Stream.Server.Listener onRequest(Stream.Server stream, HeadersFrame frame) {
        respond(stream, (MetaData.Request) frame.getMetaData());
        return null;
    }

    private void respond(Stream.Server stream, MetaData.Request request) {
        // Prepare the response HEADERS frame.
        // The response HTTP status and HTTP headers.
        MetaData.Response response = new MetaData.Response(
            HttpStatus.OK_200,
            "OK",
            HttpVersion.HTTP_3,
            HttpFields.EMPTY
        );

        if (HttpMethod.GET.is(request.getMethod())) {
            // The response content.
            // Send the HEADERS frame with the response status and headers,
            // and a DATA frame with the response content bytes
            String responseBody = "Hello world from http/3!";
            stream.respond(new HeadersFrame(response, false))
                .thenCompose(s ->
                    s.data(
                        new DataFrame(
                            ByteBuffer.wrap(responseBody.getBytes(Charset.defaultCharset())),   //  response body
                            true
                        )
                    )
                );
        } else {
            // do something when the request method is not a `GET`
            // Send just the HEADERS frame with the response status and headers.
            stream.respond(new HeadersFrame(response, true));
        }
    }
}
