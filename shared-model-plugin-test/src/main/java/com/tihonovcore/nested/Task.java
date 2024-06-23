package com.tihonovcore.nested;

import com.tihonovcore.SharedModel;
import lombok.Data;

import java.util.UUID;

@Data
@SharedModel
public class Task {

    private UUID id;
    private String name;
    private Context context;
}
