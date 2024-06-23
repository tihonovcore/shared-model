package com.tihonovcore.inheritance;

import com.tihonovcore.SharedModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SharedModel
public class B extends A {

    private int z;
}