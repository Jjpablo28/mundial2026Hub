package co.edu.unbosque.mundialhub.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Objeto de Transferencia de Datos que mapea exactamente la respuesta
 * de la API externa de generación de códigos QR.
 */
public class QrDTO {

    private String uuid;

    @SerializedName("qr_base64")
    private String qrBase64;

    @SerializedName("qr_content")
    private String qrContent;

    public QrDTO() {
    }

    public QrDTO(String uuid, String qrBase64, String qrContent) {
        this.uuid = uuid;
        this.qrBase64 = qrBase64;
        this.qrContent = qrContent;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getQrBase64() {
        return qrBase64;
    }

    public void setQrBase64(String qrBase64) {
        this.qrBase64 = qrBase64;
    }

    public String getQrContent() {
        return qrContent;
    }

    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }
}