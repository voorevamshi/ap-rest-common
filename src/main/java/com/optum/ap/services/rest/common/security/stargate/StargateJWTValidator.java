package com.optum.ap.services.rest.common.security.stargate;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tpa.ap.common.service.util.ESAPIUtil;
import com.tpa.ap.common.service.util.OapYamlParams;
import com.tpa.ap.common.service.util.ValidatorTypes;

public class StargateJWTValidator {


    private static final Logger LOGGER = Logger.getLogger(StargateJWTValidator.class);

    /**
     * Used to validate a Stargate JWT. JWT tokens are base64 encoded string, https://jwt.io to learn more
     *
     *
     * @param {String} token - the raw JWT token found in HTTP Header "JWT"
     * @param {InputStream} requestPayload - an inputstream of the request payload found in the HTTP body
     * @param {KeyStore} trustStore - a loaded instance of your truststore (KeyStore ks; ks.load(keyStoreInputStream, passwordCharArray);)
     * @throws StargateJWTException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws IOException
     */
    public DecodedJWT validateJWTToken(String token, InputStream requestPayload, KeyStore trustStore)
            throws StargateJWTException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            CertificateException,
            KeyStoreException,
            IOException {
        this.validate(token, requestPayload, trustStore, false);
        return JWT.decode(token);
    }

    /**
     * Used to validate a Stargate JWT. JWT tokens are base64 encoded string, https://jwt.io to learn more
     *
     *
     * @param {String} token - the raw JWT token found in HTTP Header "JWT"
     * @param {InputStream} requestPayload - an inputstream of the request payload found in the HTTP body
     * @param {KeyStore} trustStore - a loaded instance of your truststore (KeyStore ks; ks.load(keyStoreInputStream, passwordCharArray);)
     * @param {Boolean} ignorePayloadValidation - set TRUE to skip validating of payload hash inputstream. set FALSE to validate payload hash inputstream
     * @throws StargateJWTException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws IOException
     */
	public void validate(String token, InputStream requestBody, KeyStore trustStore, Boolean ignorePayloadValidation)
			throws StargateJWTException,
			NoSuchAlgorithmException,
			InvalidKeySpecException,
			CertificateException,
			KeyStoreException,
			IOException {
		// Decode the unauthenticated JWT to retrieve the x509 cert
		X509Certificate cert = extractCertFromJWT(token);

		// Validate x509 cert against our truststore
		validateTrustedCertificate(trustStore, cert);

		PublicKey publicKey = cert.getPublicKey(); // Instantiates public key for token validation

        try {
			int leeway = OapYamlParams.getInteger("jwt.leeway", 1);
      	  
            // Validate signature and expiry on JWT
            DecodedJWT jwt = JWT.require(Algorithm.RSA256((RSAPublicKey) publicKey, null)).acceptLeeway(leeway).build().verify(token); // Verify JWT with public key
            // Compared HTTP Request body hash with Payload hash claim
            if (!ignorePayloadValidation) validatePayloadHash(requestBody, jwt);

		} catch(JWTVerificationException exception) {
			LOGGER.error(exception.getMessage());
			throw new StargateJWTException(exception.getMessage() + "\n" + token);
		}

	}

	private void validatePayloadHash(InputStream requestBody, DecodedJWT jwt)
			throws IOException,
			StargateJWTException {
		String requestBodyHash = DigestUtils.sha256Hex(requestBody);
		String jwtBodyHash = jwt.getClaim("payloadhash").asString();
		if (! (StringUtils.isEmpty(requestBodyHash) && StringUtils.isEmpty(jwtBodyHash))) {
			if (!requestBodyHash.equals(jwtBodyHash)) { // Compare hash of payload to payloadhash from JWT
				throw new StargateJWTException("Payload validation failure - 'payloadhash' JWT claim (" + jwtBodyHash + ")is not equal to the hash of the request body (" + requestBodyHash + ")");
			}
		}
	}

    private X509Certificate extractCertFromJWT(String token) throws CertificateException {
        DecodedJWT unauthenticatedJWT = JWT.decode(token);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        String encodedCert = unauthenticatedJWT.getHeaderClaim("x5c").asArray(String.class)[0];
        InputStream in =new ByteArrayInputStream(Base64.decodeBase64(encodedCert.getBytes()));
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate( in );
        return cert;
    }

	private void validateTrustedCertificate(KeyStore trustStore, X509Certificate cert)
			throws NoSuchAlgorithmException,
			KeyStoreException,
			StargateJWTException {

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		trustManagerFactory.init(trustStore);
		for (TrustManager trustManager: trustManagerFactory.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager) {
				X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
				// If the certificate is trusted by the trust manager, do nothing - else throw exception
				try {

					x509TrustManager.checkServerTrusted(new X509Certificate[] {
							cert
					},
							"RSA");
				} catch(Exception e) {
					throw new StargateJWTException(e.getMessage() + "\nUnable to validate certificate included in JWT\n[" + cert.toString() + "]");
				}
			}
		}
	}

	public class StargateJWTException extends Exception {
		private static final long serialVersionUID = 1L;
		public StargateJWTException(String message) {
			super(message);
		}
	}
}
