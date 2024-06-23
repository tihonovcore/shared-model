package com.tihonovcore.nested;

import com.tihonovcore.SharedModel;
import lombok.Data;

@Data
@SharedModel
public class Context {

    private String owner;
    private String startTime;
}
