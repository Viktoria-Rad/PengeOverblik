package app.security;

import app.services.TokenService;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityMiddleware {

    private final TokenService tokenService;

    public void validateToken(Context ctx) {

        String token = null;

        String authorizationHeader = ctx.header("Authorization");

        if (authorizationHeader != null &&
                authorizationHeader.startsWith("Bearer ")) {

            token = authorizationHeader.substring(7);
        }

        if (token == null) {
            token = ctx.cookie("token");
        }

        if (token == null) {
            throw new UnauthorizedResponse("Missing or invalid token");
        }

        try {
            Integer userId =
                    tokenService.getUserIdFromToken(token);

            ctx.attribute("userId", userId);

        } catch (Exception e) {
            throw new UnauthorizedResponse("Invalid or expired token");
        }
    }
}