package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractMethodToken {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("method")
    private String method;

    @JsonProperty("paramcount")
    private Integer paramCount;

    @JsonProperty("hasreturnvalue")
    private Boolean returnValue;

    @JsonProperty("callflags")
    private String callFlags;

    public ContractMethodToken() {
    }

    public ContractMethodToken(String hash, String method, Integer paramCount,
            Boolean returnValue, String callFlags) {
        this.hash = hash;
        this.method = method;
        this.paramCount = paramCount;
        this.returnValue = returnValue;
        this.callFlags = callFlags;
    }

    public String getHash() {
        return hash;
    }

    public String getMethod() {
        return method;
    }

    public Integer getParamCount() {
        return paramCount;
    }

    public Boolean hasReturnValue() {
        return returnValue;
    }

    public String getCallFlags() {
        return callFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractMethodToken)) {
            return false;
        }
        ContractMethodToken that = (ContractMethodToken) o;
        return Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getMethod(), that.getMethod()) &&
                Objects.equals(getParamCount(), that.getParamCount()) &&
                Objects.equals(returnValue, that.returnValue) &&
                Objects.equals(getCallFlags(), that.getCallFlags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHash(), getMethod(), getParamCount(), returnValue, getCallFlags());
    }

    @Override
    public String toString() {
        return "ContractMethodToken{" +
                "hash='" + hash + '\'' +
                ", method='" + method + '\'' +
                ", paramCount=" + paramCount +
                ", returnValue=" + returnValue +
                ", callFlags='" + callFlags + '\'' +
                '}';
    }
}
