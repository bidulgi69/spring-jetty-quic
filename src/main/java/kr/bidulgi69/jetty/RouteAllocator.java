package kr.bidulgi69.jetty;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.http3.api.Session;
import org.eclipse.jetty.http3.api.Stream;
import org.eclipse.jetty.http3.frames.HeadersFrame;

public class RouteAllocator implements Session.Server.Listener {

    private final HelloListener helloListener;
    private final ImageListener imageListener;

    public RouteAllocator() {
        this.helloListener = new HelloListener();
        this.imageListener = new ImageListener();
    }

    @Override
    public void onAccept(Session session) {
        // called when the request is accepted
    }

    @Override
    public Stream.Server.Listener onRequest(Stream.Server stream, HeadersFrame frame) {
        // Send a response after reading the request.
        MetaData.Request request = (MetaData.Request) frame.getMetaData();
        Session.Server.Listener delegate;

        delegate = switch (request.getHttpURI().getPath()) {
            case "/" -> helloListener;
            case "/image" -> imageListener;
            default -> null;
        };

        if (delegate == null) {
            // Handle non-existing resources
            MetaData.Response response = new MetaData.Response(
                HttpStatus.NOT_FOUND_404,
                "Not Found",
                HttpVersion.HTTP_3,
                HttpFields.EMPTY
            );
            stream.respond(new HeadersFrame(response, true));
            return null;
        }

        if (frame.isLast()) {
            return delegate.onRequest(stream, frame);
        } else {
            // When a request is sent as stream(HTTP/2, HTTP/3)
            // Demand to be called back when data is available.
            stream.demand();
            return new Stream.Server.Listener() {
                @Override
                public void onDataAvailable(Stream.Server stream) {
                    Stream.Data data = stream.readData();
                    if (data == null) {
                        stream.demand();
                    } else {
                        // Consume the request content.
                        if (data.isLast()) {
                            delegate.onRequest(stream, frame);
                        }
                    }
                }
            };
        }
    }
}
