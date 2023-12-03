package net.queencoder.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CbaApiResponse {

    private Data data;
    private Modifier modifier;
    private Realm realm;
    private Collection collection;
    private String id;
    @JsonProperty("created_date")
    private String createdDate;
    @JsonProperty("modified_date")
    private String modifiedDate;
    private String uuid;
    private User user;
    @JsonProperty("entity_tag")
    private String entityTag;
    private String status;

    // getters and setters

    // Inner classes representing nested structures
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String code;
        private String description;

        // getters and setters
    }

    public static class Modifier {
        private String name;
        private String id;
        private String uuid;
        private String username;

        // getters and setters
    }

    public static class Realm {
        private String id;

        // getters and setters
    }

    public static class Collection {
        private String name;
        private String id;
        private String slug;

        // getters and setters
    }

    public static class User {
        private String name;
        private String id;
        private String username;

        // getters and setters
    }
}
