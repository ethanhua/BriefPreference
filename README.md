# BriefPreference
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

A library provides an easy way to use SharePreferences like Retrofit and support reactive  

[文章](https://ethanhua.github.io/2018/02/27/briefpreference/)
## Feature
- [x] Retrofit Style
- [x] Support multiple SharePreferences
- [x] Support reactive
- [x] Support default Object value
- [x] Support custom Serializable
## Getting stated
In your build.gradle:
```
implementation 'com.ethanhua:briefpreference:0.0.1' 
```
## Usage

**define api interface and data repository**

```Kotlin
interface UserService {

    fun putName(value: String)

    fun getName(): String

}

object UserRepository {

    private val localService: UserService by lazy {
        BriefPreference().create(AppContext.instance, UserService::class.java)
    }

    fun putUserName(name: String) = localService.putName(name)

    fun getUserName() = localService.getName()
}
```
**use it in your activity or fragment** 

```
text.text = UserRepository.getUserName()

```

### Advanced usage

**@SpName**

> define the SharePreferences file name. the default name is the interface name
```Kotlin
@SpName("user_preferences")
interface UserService

```
**@Key**

> define the KeyName of SharePreference. the default name is extracted from the method name

```Kotlin
interface UserService {
    @Key("testName") 
    fun putName(value: String)
    
    @Key("testName") 
    fun getName(): String
}
```
**@Default** 

> define the get Action default return value. can not be null if you use reactive mode

```Kotlin
interface UserService {
    @Key("testName") 
    fun putName(value: String)
    
    @Key("testName") 
    fun getName(@Default defaultName: String = "ethanhua"): String
}

```
**@Clear** **@Remove**

> define the clear and remove Action

**Serializable**

> BriefPreference default support Serializable and Parcelable  
if you need custom serializable mode can implements Converter like:

```Kotlin
class GsonConverterFactory : Converter.Factory {

    private val gson = Gson()

    override fun <F> fromType(fromType: Type): Converter<F, String> {
        return object : Converter<F, String> {
            override fun convert(value: F?): String? {
                return gson.toJson(value)
            }
        }
    }

    override fun <T> toType(toType: Type): Converter<String, T> {
        return object : Converter<String, T> {
            override fun convert(value: String?): T? {
                return gson.fromJson(value, toType)
            }
        }
    }

}
class UserRepository{
    private val localService: UserService by lazy {
            BriefPreference(GsonConverterFactory()).create(AppContext.instance, UserService::class.java)
        }
}
```

**reactive**

> you can implements reactive program in SharePreference
```Kotlin
    fun listUser(@Default listUser: MutableList<User> = mutableListOf()): Observable<MutableList<User>>

    fun updateUser(listUser: MutableList<User>)
    
    fun use(){
        UserRepository.listUser().subscribe({
                    it?.let {
                        if(it.isNotEmpty()){
                            text.text = it[0].userName
                        }
                    }
                }, {
                    it.printStackTrace()
                })
        
        btn.setOnClickListener({
                    val name = edit.text.toString()
                    val user = User(name, "avatar")
                    UserRepository.updateListUser(mutableListOf(user))
                })
    }  
``` 
## License

[**The MIT License**](http://opensource.org/licenses/MIT).
