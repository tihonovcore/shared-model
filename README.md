## Shared model

Consider there are several services that exchange information between each other.
They all use for it the same model, for example:
```java
@Data
public class User {

    private String name;
    private int age;
    private List<String> phoneNumbers;
    private List<Address> addresses;
    //...
}
```
Every single service **must** have the latest version of this model, even if it doesn't use update fields.
Consider service A sends data to service B, and B changes the field `age` and sends record to C.
And we want to add new field `boolean flag` that is used by A and C. 
The model must be updated in all services, including service B, to prevent data loss.

To reduce this annoying work and for simplifying model definition use `@SharedModel`:
```java
@Data
@SharedModel
public class User {

    private int age;
}
```
While deserialization `age` from input-bytes will be set to `User.age`, 
and all other fields will be set in hidden field `_unusedFields`.
While serialization `User.age` goes into input-bytes as it is,
and all will be taken from hidden field `_unusedFields`.


## How it works

It works thanks to bytecode instrumentation via asm ow2 library (https://asm.ow2.io/asm4-guide.pdf). 
And we get code like this:
```java
import java.util.HashMap;

@Data
public class User {

    @JsonIgnore
    private Map<String, Object> _unusedFields;

    private int age;

    @JsonAnyGet
    public Map _anyGet() {
        if (_unusedFields == null) {
            this._unusedFields = new HashMap();
        }
        return _unusedFields;
    }
    
    @JsonAnySet
    public void _anySet(String key, Object value) {
        if (_unusedFields == null) {
            this._unusedFields = new HashMap();
        }
        _unusedFields.put(key, value);
    }
}
```