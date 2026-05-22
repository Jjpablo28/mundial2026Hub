package co.edu.unbosque.mundialhub.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import co.edu.unbosque.mundialhub.dto.QrDTO;


public class ExternalHTTPRequestHandler {
	/**
	 * Cliente HTTP singleton configurado con HTTP/2 y un timeout de conexión.
	 */
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(10)).build();

	/**
	 * Realiza una petición GET a la URL especificada y parsea la respuesta JSON
	 * para imprimirla de forma legible utilizando Gson.
	 *
	 * @param url La URL a la que se realizará la petición GET.
	 * @return Una cadena JSON formateada de forma legible.
	 */
	public static String doGetAndParse(String url) {
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url))
				.header("Content-type", "application/json").build();

		HttpResponse<String> response = null;

		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("status code -> " + response.statusCode());
		String uglyJson = response.body();
		return prettyPrintUsingGson(uglyJson);
	}

	/**
	 * Formatea una cadena JSON de forma legible utilizando la librería Gson.
	 *
	 * @param uglyJson La cadena JSON sin formato.
	 * @return Una cadena JSON formateada de forma legible.
	 */
	public static String prettyPrintUsingGson(String uglyJson) {
		@SuppressWarnings("deprecation")
		Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
		JsonElement jsonElement = JsonParser.parseString(uglyJson);
		String prettyJsonString = gson.toJson(jsonElement);
		return prettyJsonString;

	}
	
	public static String doPost(String url, String json) {
		HttpRequest solicitud = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json))
				.uri(URI.create(url)).header("Content-Type", "application/json").build();
		HttpResponse<String> response = null;
		try {
			response = HTTP_CLIENT.send(solicitud, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		
			e.printStackTrace();
		}

		return String.valueOf(response.statusCode());
	}
	
	public static String doPut(String url, String json) {
		HttpRequest solicitud = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(json))
				.uri(URI.create(url)).header("Content-Type", "application/json").build();
		HttpResponse<String> response = null;
		try {
			response = HTTP_CLIENT.send(solicitud, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response.statusCode());
		return String.valueOf(response.statusCode());
	}

	public static String doDelete(String url) {
		HttpRequest solicitud = HttpRequest.newBuilder().DELETE().uri(URI.create(url))
				.header("Content-Type", "application/json").build();

		HttpResponse<String> response = null;
		try {
			response = HTTP_CLIENT.send(solicitud, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return String.valueOf(response.statusCode());
	}
	
	/**
	 * Realiza una petición GET a la URL especificada incluyendo un token de autenticación.
	 *
	 * @param url La URL a la que se realizará la petición GET.
	 * @param token El valor del X-Auth-Token.
	 * @return Una cadena JSON formateada.
	 */
	public static String doGetWithAuthToken(String url, String token) {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.header("X-Auth-Token", token) 
				.build();

		HttpResponse<String> response = null;

		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("status code -> " + response.statusCode());
			
			// Usamos tu método existente para devolver el JSON bonito
			return prettyPrintUsingGson(response.body()); 
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		
		return null; // Retorna null en caso de que falle la petición
	}
	
	public static String doGetWithAuthTokenApiFootball(String url, String token) {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.header("x-apisports-key", token) 
				.build();

		HttpResponse<String> response = null;

		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("status code -> " + response.statusCode());
			
			// Usamos tu método existente para devolver el JSON bonito
			return prettyPrintUsingGson(response.body()); 
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		
		return null; // Retorna null en caso de que falle la petición
	}
	

	/**
	 * Realiza una petición POST a la API externa de QR y mapea automáticamente
	 * el JSON recibido al objeto QrDTO usando Gson.
	 *
	 * @param url La URL del servicio de generación de QR.
	 * @return Un objeto QrDTO con el UUID y la imagen en Base64.
	 */
	public static QrDTO getQrFromQrApi(String url) {
		// Cambiamos .GET() por .POST(HttpRequest.BodyPublishers.noBody())
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.noBody())
				.uri(URI.create(url))
				.header("Accept", "application/json")
				.build();

		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("API QR status code -> " + response.statusCode());

			if (response.statusCode() != 200 && response.statusCode() != 201) {
				throw new RuntimeException("Error al consultar API QR. Status: " + response.statusCode());
			}

			// Gson leerá las anotaciones @SerializedName para mapear el JSON correctamente
			return new Gson().fromJson(response.body(), co.edu.unbosque.mundialhub.dto.QrDTO.class);

		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Error de comunicación con la API de QR", e);
		}
	}

	
}
