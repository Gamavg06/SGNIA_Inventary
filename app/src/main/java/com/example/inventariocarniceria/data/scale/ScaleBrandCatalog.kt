package com.example.inventariocarniceria.data.scale

data class ScaleBrandModel(
    val brand: String,
    val model: String,
    val defaultPort: Int,
    val protocol: String,
    val description: String,
    val popular: Boolean = false
)

object ScaleBrandCatalog {
    val CATALOG = listOf(
        // Systel
        ScaleBrandModel("Systel", "Cuora Max", 10001, "SYSTEL_CUORA", "Báscula etiquetadora / mostrador (Protocolo Binario V7.0/V6.2)", true),
        ScaleBrandModel("Systel", "Cuora 2", 10001, "SYSTEL_CUORA", "Báscula etiquetadora Cuora 2 (Protocolo V4.0)", true),
        ScaleBrandModel("Systel", "Croma", 10001, "SYSTEL_ASCII", "Báscula comercial de peso continuo"),
        ScaleBrandModel("Systel", "Brio", 10001, "SYSTEL_ASCII", "Báscula compacta de mostrador"),

        // Torrey
        ScaleBrandModel("Torrey", "L-EQ 5/10/20", 5000, "TORREY_W", "Báscula porcionadora comercial puerto serial/Ethernet", true),
        ScaleBrandModel("Torrey", "W-LABEL 20/40", 5000, "TORREY_CONTINUOUS", "Báscula etiquetadora industrial con salida de red", true),
        ScaleBrandModel("Torrey", "PCR 20/40", 5000, "TORREY_P", "Báscula de mostrador para carnicerías", true),
        ScaleBrandModel("Torrey", "MFQ 20/40", 5000, "TORREY_W", "Báscula industrial de precisión"),
        ScaleBrandModel("Torrey", "L-PC", 5000, "TORREY_P", "Báscula comercial de peso y precio"),

        // Rhino
        ScaleBrandModel("Rhino", "BAR-8", 10001, "RHINO_CONTINUOUS", "Báscula comercial compacta para mostrador", true),
        ScaleBrandModel("Rhino", "BAR-9", 10001, "RHINO_CONTINUOUS", "Báscula digital alta precisión con salida TCP/IP", true),
        ScaleBrandModel("Rhino", "BAC-300 / BAP-300", 10001, "RHINO_CONTINUOUS", "Báscula de plataforma para canal y recibo de carne", true),
        ScaleBrandModel("Rhino", "I-QY Industrial", 10001, "RHINO_CONTINUOUS", "Indicador industrial de acero inoxidable"),

        // Mettler Toledo
        ScaleBrandModel("Mettler Toledo", "Toledo 8217 / MT-SICS", 10001, "TOLEDO_SICS", "Báscula empotrada para punto de venta", true),
        ScaleBrandModel("Mettler Toledo", "Tiger 15D / 30D", 10001, "TOLEDO_CONTINUOUS", "Báscula etiquetadora de carnicería"),
        ScaleBrandModel("Mettler Toledo", "Viva / FreshBase", 10001, "TOLEDO_SICS", "Báscula electrónica para autoservicio"),

        // CAS
        ScaleBrandModel("CAS", "PD-II POS", 10001, "CAS_STANDARD", "Báscula de interfase POS de alta velocidad", true),
        ScaleBrandModel("CAS", "AP-1 / ER-Plus", 10001, "CAS_STANDARD", "Báscula de mostrador comercial"),
        ScaleBrandModel("CAS", "CL-5000 / CL-5200", 10001, "CAS_STANDARD", "Báscula etiquetadora de red Ethernet"),

        // Dibatec / Bizerba
        ScaleBrandModel("Dibatec", "D900 / Industrial", 10001, "DIBATEC_TCP", "Báscula colgante y de mostrador"),
        ScaleBrandModel("Bizerba", "SC-II / XC", 10001, "BIZERBA_ETH", "Báscula PC industrial de mostrador"),

        // Indicadores Industriales Genéricos
        ScaleBrandModel("Yaohua", "XK3190-A12E", 10001, "GENERIC_CONTINUOUS", "Indicador de plataforma para recepción de carne", true),
        ScaleBrandModel("Genérica", "TCP/IP Continuo (ASCII)", 10001, "AUTO", "Cualquier báscula con convertidor Ethernet/Wi-Fi estándar", true)
    )

    val BRANDS = listOf("TODAS", "Systel", "Torrey", "Rhino", "Mettler Toledo", "CAS", "Dibatec", "Yaohua", "Genérica")
}
