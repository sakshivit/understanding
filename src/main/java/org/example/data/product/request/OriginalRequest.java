package org.example.data.product.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OriginalRequest {

    public String businessname;
    public Object address1;
    public Object address2;
    public String city;
    public String state;
    public String zipCode;
    public String websiteUrl;
    public String expSic;
}
