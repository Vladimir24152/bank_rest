package com.example.bankcards.constant;

public final class ApiConstant {

    private ApiConstant() {
    }

    public static final String EXAMPLE_URL = "/api/v1/example";

    public static final String AUTH_BASE_URL = "/api/v1/auth";

    public static final String CARD_BASE_URL = "/api/v1/card";

    public static final String CREATE = "/create";

    public static final String GET_ALL = "/get-all";

    public static final String UPDATE = "/update";

    public static final String BLOCKED = "/blocked";

    public static final String BALANCE = "/balance";

    public static final String TRANSFER = "/transfer";

    public static final String SIGN_UP = "/sign-up";

    public static final String SIGN_IN = "/sign-in";

    public static final String GIVE_ADMIN = "/give-admin";

    public static final String CARD_NUMBER = "/{cardNumber}";

    public static final String USER_ID = "/user/{userId}";

    public static final String CARD_ID = "/{cardId}";
}