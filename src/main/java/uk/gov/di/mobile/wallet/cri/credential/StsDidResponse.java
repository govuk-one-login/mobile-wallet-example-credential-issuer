// package uk.gov.di.mobile.wallet.cri.credential;
//
// import com.fasterxml.jackson.annotation.JsonProperty;
//
// import java.util.List;
//
// public class StsDidResponse {
//    public StsDidResponse() {}
//
//    @JsonProperty("@context")
//    List<String> context;
//
//    @JsonProperty("id")
//    String id;
//
//    @JsonProperty("verificationMethod")
//    List<VerificationMethod> verificationMethod;
//
//    @JsonProperty("assertionMethod")
//    List<String> assertionMethod;
//
//    public List<String> getContext() {
//        return context;
//    }
//
//    public void setContext(List<String> context) {
//        this.context = context;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public List<VerificationMethod> getVerificationMethod() {
//        return verificationMethod;
//    }
//
//    public void setVerificationMethod(List<VerificationMethod> verificationMethod) {
//        this.verificationMethod = verificationMethod;
//    }
//
//    public List<String> getAssertionMethod() {
//        return assertionMethod;
//    }
//
//    public void setAssertionMethod(List<String> assertionMethod) {
//        this.assertionMethod = assertionMethod;
//    }
//
//    public static class VerificationMethod {
//
//        public VerificationMethod() {}
//
//        @JsonProperty("id")
//        String id;
//
//        @JsonProperty("type")
//        String type;
//
//        @JsonProperty("controller")
//        String controller;
//
//        @JsonProperty("publicKeyJwk")
//        PublicKeyJwk publicKeyJwk;
//
//        public String getId() {
//            return id;
//        }
//
//        public void setId(String id) {
//            this.id = id;
//        }
//
//        public String getType() {
//            return type;
//        }
//
//        public void setType(String type) {
//            this.type = type;
//        }
//
//        public String getController() {
//            return controller;
//        }
//
//        public void setController(String controller) {
//            this.controller = controller;
//        }
//
//        public PublicKeyJwk getPublicKeyJwk() {
//            return publicKeyJwk;
//        }
//
//        public void setPublicKeyJwk(PublicKeyJwk publicKeyJwk) {
//            this.publicKeyJwk = publicKeyJwk;
//        }
//
//        public static class PublicKeyJwk {
//
//            public PublicKeyJwk() {}
//
//            @JsonProperty("kty")
//            String kty;
//
//            @JsonProperty("n")
//            String n;
//
//            @JsonProperty("e")
//            String e;
//
//            @JsonProperty("kid")
//            String kid;
//
//            public String getKty() {
//                return kty;
//            }
//
//            public void setKty(String kty) {
//                this.kty = kty;
//            }
//
//            public String getN() {
//                return n;
//            }
//
//            public void setN(String n) {
//                this.n = n;
//            }
//
//            public String getE() {
//                return e;
//            }
//
//            public void setE(String e) {
//                this.e = e;
//            }
//
//            public String getKid() {
//                return kid;
//            }
//
//            public void setKid(String kid) {
//                this.kid = kid;
//            }
//        }
//    }
// }
