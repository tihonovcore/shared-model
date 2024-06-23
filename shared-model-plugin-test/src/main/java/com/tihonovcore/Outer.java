package com.tihonovcore;

import lombok.Data;

@Data
@SharedModel
public class Outer {

    private String foo;
    private Inner inner;

    @Data
    @SharedModel
    public static class Inner {

        private String bar;
    }
}
