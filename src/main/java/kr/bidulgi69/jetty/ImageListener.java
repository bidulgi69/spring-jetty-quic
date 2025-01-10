package kr.bidulgi69.jetty;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.http3.api.Session;
import org.eclipse.jetty.http3.api.Stream;
import org.eclipse.jetty.http3.frames.DataFrame;
import org.eclipse.jetty.http3.frames.HeadersFrame;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageListener implements Session.Server.Listener {

    @Override
    public Stream.Server.Listener onRequest(Stream.Server stream, HeadersFrame frame) {
        MetaData.Request request = (MetaData.Request) frame.getMetaData();
        if (!HttpMethod.GET.name().equals(request.getMethod())) {
            MetaData.Response response = new MetaData.Response(
                HttpStatus.METHOD_NOT_ALLOWED_405,
                "HttpMethod " + request.getMethod() + " is not allowed",
                HttpVersion.HTTP_3,
                HttpFields.EMPTY
            );
            stream.respond(new HeadersFrame(response, true));
            return null;
        }

        String queryString = request.getHttpURI().getQuery();
        String imageName = queryString.substring(queryString.indexOf("=") + 1);
        ClassPathResource imageResource = new ClassPathResource(imageName);
        if (imageResource.exists()) {
            try {
                byte[] imageBytes = imageResource.getContentAsByteArray();
                MetaData.Response response = new MetaData.Response(
                    HttpStatus.OK_200,
                    "OK",
                    HttpVersion.HTTP_3,
                    HttpFields.from(
                        new HttpField(HttpHeader.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", imageName)),
                        new HttpField(HttpHeader.CONTENT_LENGTH, String.valueOf(imageBytes.length)),
                        new HttpField(HttpHeader.CONTENT_TYPE, "application/octet-stream")
                    )
                );
                stream.respond(new HeadersFrame(response, false))
                    .thenCompose(s ->
                        s.data(
                            new DataFrame(
                                ByteBuffer.wrap(imageBytes),
                                true
                            )
                        )
                    );
            } catch (IOException e) {
                MetaData.Response response = new MetaData.Response(
                    HttpStatus.INTERNAL_SERVER_ERROR_500,
                    e.getMessage(),
                    HttpVersion.HTTP_3,
                    HttpFields.EMPTY
                );
                stream.respond(new HeadersFrame(response, true));
            }
        }
        return null;
    }
}
