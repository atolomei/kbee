
package kbee.api.model;
public class ApiRefreshResponse {

    private ApiToken token;
    private String refreshToken;
    private long expiresIn;

    public ApiToken getToken() {
        return token;
    }

    public void setToken(ApiToken token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}