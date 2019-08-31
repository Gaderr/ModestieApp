package com.modestie.modestieapp.utils.network;

/**
 * This abstract class stores the endpoints used for HTTP requests.
 */
public abstract class RequestURLs
{
    // ---
    // VARS
    // ---

    // Modestie free company ID
    public static final String MODESTIE_FC_ID = "9232660711086299979";


    // ---
    // DOMAINS
    // ---

    // XIVAPI REST API
    public static final String XIVAPI_DOMAIN = "https://xivapi.com";
    public static final String XIVAPI_DOMAIN_STAGING = "https://staging.xivapi.com";
    // Modestie Events REST API
    public static final String MODESTIE_DOMAIN = "https://modestie.fr";


    // ---
    // REQUESTS
    // ---

    // --- XIVAPI REST API

    // Global parameters
    public static final String XIVAPI_LANGUAGE_FR_PARAM = "language=fr";

    // Free company
    public static final String XIVAPI_FREECOMPANY_REQ = XIVAPI_DOMAIN + "/freecompany";
    public static final String XIVAPI_FREECOMPANY_PARAM_FCM = "?data=FCM";

    // Characters
    public static final String XIVAPI_CHARACTER_REQ = XIVAPI_DOMAIN + "/character";
    public static final String XIVAPI_CHARACTER_PARAM_EXTENDED = "?language=fr&extended=1";
    public static final String XIVAPI_CHARACTER_PARAM_LIGHT = "?columns=Character.Name,Character.Avatar,Character.ID,Character.Server&language=fr";
    public static final String XIVAPI_CHARACTER_PARAM_BIO = "?columns=Character.Bio";
    public static final String XIVAPI_CHARACTER_PARAM_FREECOMPANY_ID = "?columns=Character.FreeCompanyId";
    public static final String XIVAPI_CHARACTER_EXT_SEARCH = "/search";
    public static final String XIVAPI_CHARACTER_EXT_SEARCH_SERVER_PARAM = "server=";
    public static final String XIVAPI_CHARACTER_EXT_SEARCH_NAME_PARAM = "name=";

    // Search
    public static final String XIVAPI_SEARCH_REQ = XIVAPI_DOMAIN + "/search";
    public static final String XIVAPI_SEARCH_INDEX_PARAM = "indexes=";
    public static final String XIVAPI_SEARCH_PAGE_PARAM = "page=";
    public static final String XIVAPI_SEARCH_STRING_PARAM = "string=";

    // --- IMGUR REST API

    // Image upload
    public static final String IMGUR_IMG_UPLOAD = "https://api.imgur.com/3/upload";
    public static final String IMGUR_TAG_IMAGE = "image";
    public static final String IMGUR_TAG_TYPE = "type";
    public static final String IMGUR_TAG_TITLE = "title";
    public static final String IMGUR_TAG_NAME = "name";
    public static final String IMGUR_ALBUM_HASH = "jLDde4Z";
    public static final String IMGUR_CLIENT_ID = "1a437e09e459eab";

    // --- MODESTIE JWT AUTH (WP plugin)

    // Get JWT
    public static final String MODESTIE_JWT_GET_REQ = MODESTIE_DOMAIN + "/wp-json/jwt-auth/token";

    // Validate JWT
    public static final String MODESTIE_JWT_VALIDATE_REQ = MODESTIE_JWT_GET_REQ + "/validate";

    // --- MODESTIE EVENTS REST API

    // Base endpoint
    private static final String MODESTIE_REST_ENDPOINT = MODESTIE_DOMAIN + "/wp-json/modestieevents/v1";
    private static final String MODESTIE_REST_ENDPOINT_ADD = "/add";
    private static final String MODESTIE_REST_ENDPOINT_UPDATE = "/update";
    private static final String MODESTIE_REST_ENDPOINT_REMOVE = "/remove";

    // Participants
    public static final String MODESTIE_PARTICIPANTS_REQ = MODESTIE_REST_ENDPOINT + "/participants";
    public static final String MODESTIE_PARTICIPANTS_ADD = MODESTIE_PARTICIPANTS_REQ + MODESTIE_REST_ENDPOINT_ADD;
    public static final String MODESTIE_PARTICIPANTS_REMOVE = MODESTIE_PARTICIPANTS_REQ + MODESTIE_REST_ENDPOINT_REMOVE;
}
