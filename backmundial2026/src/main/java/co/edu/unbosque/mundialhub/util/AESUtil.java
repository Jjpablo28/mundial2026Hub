package co.edu.unbosque.mundialhub.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de utilidad criptográfica para operaciones de cifrado simétrico AES y
 * funciones de resumen (hashing).
 * <p>
 * Implementa el modo de operación <b>AES/GCM/NoPadding</b>, el cual proporciona
 * tanto confidencialidad como autenticación de los datos, siendo una práctica
 * recomendada en seguridad moderna. Adicionalmente, integra métodos para
 * generar huellas digitales (hashes) mediante diversos algoritmos (MD5, SHA-1,
 * SHA-256, etc.) utilizando Apache Commons Codec.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
public class AESUtil {

	/** Identificador del algoritmo simétrico estándar AES. */
	private final static String ALGORITMO = "AES";

	/**
	 * Especificación del modo GCM (Galois/Counter Mode) sin relleno (NoPadding).
	 */
	private final static String TIPOCIFRADO = "AES/GCM/NoPadding";

	/**
	 * Encripta un texto plano utilizando una clave secreta y un vector de
	 * inicialización (IV).
	 *
	 * @param llave La clave de cifrado (Secret Key).
	 * @param iv    El vector de inicialización (IV) para garantizar la aleatoriedad
	 *              del cifrado.
	 * @param texto El contenido de texto plano a encriptar.
	 * @return El texto cifrado codificado en formato Base64.
	 */
	public static String encrypt(String llave, String iv, String texto) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TIPOCIFRADO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		byte[] encrypted = null;
		try {
			encrypted = cipher.doFinal(texto.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}

		return new String(encodeBase64(encrypted));
	}

	/**
	 * Desencripta un texto cifrado en Base64 utilizando la clave y el IV
	 * proporcionados.
	 *
	 * @param llave     La clave secreta original utilizada para el cifrado.
	 * @param iv        El vector de inicialización (IV) utilizado durante el
	 *                  cifrado.
	 * @param encrypted El texto cifrado en formato Base64.
	 * @return El texto plano original desencriptado.
	 */
	public static String decrypt(String llave, String iv, String encrypted) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TIPOCIFRADO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		byte[] enc = decodeBase64(encrypted);
		byte[] decrypted = null;
		try {
			decrypted = cipher.doFinal(enc);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}

		return new String(decrypted);
	}

	/**
	 * Desencripta un texto utilizando parámetros de clave e IV predefinidos en la
	 * clase.
	 *
	 * @param encrypted El texto cifrado en formato Base64.
	 * @return El texto plano resultante.
	 */
	public static String decrypt(String encrypted) {
		String iv = "worldcup2026hub";
		String key = "keywhitsecuredlk";
		return decrypt(key, iv, encrypted);
	}

	/**
	 * Encripta un texto utilizando parámetros de clave e IV predefinidos en la
	 * clase.
	 *
	 * @param plainText El texto plano a proteger.
	 * @return El texto encriptado en formato Base64.
	 */
	public static String encrypt(String plainText) {
		String iv = "worldcup2026hub";
		String key = "keywhitsecuredlk";
		return encrypt(key, iv, plainText);
	}

	/** @return Hash MD5 (128 bits) en formato hexadecimal. */
	public static String hashingToMD5(String content) {
		return DigestUtils.md5Hex(content);
	}

	/** @return Hash SHA-1 (160 bits) en formato hexadecimal. */
	public static String hashingToSHA1(String content) {
		return DigestUtils.sha1Hex(content);
	}

	/** @return Hash SHA-256 (256 bits) en formato hexadecimal. */
	public static String hashingToSHA256(String content) {
		return DigestUtils.sha256Hex(content);
	}

	/** @return Hash SHA-384 (384 bits) en formato hexadecimal. */
	public static String hashingToSHA384(String content) {
		return DigestUtils.sha384Hex(content);
	}

	/** @return Hash SHA-512 (512 bits) en formato hexadecimal. */
	public static String hashingToSHA512(String content) {
		return DigestUtils.sha512Hex(content);
	}

	public static void main(String[] args) {
		System.out.println(decrypt("M33oLCd+9hK4olsKCaW2t+HO1Czd8NJQFw=="));
	}
}