package com.wt.pinger.providers.data;

import com.wt.pinger.proto.DataFieldAnnotation;
import com.wt.pinger.proto.ItemProto;

/**
 * Created by Kenumir on 2016-09-01.
 *
 */
public class AddressItem extends ItemProto {

    @Deprecated
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ADDRESS = "addres";
    public static final String FIELD_PACKET = "packet";
    public static final String FIELD_PINGS = "pings";
    public static final String FIELD_DISPLAY_NAME = "display_name";

    @Deprecated
    @DataFieldAnnotation public String name; // not used
    @DataFieldAnnotation public String addres;
    @DataFieldAnnotation public Integer packet;
    @DataFieldAnnotation public Integer pings;
    @DataFieldAnnotation public String display_name;

}
