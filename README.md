# grpc-android-server-demo
Use Android Java to implement grpc server and golang to implement grpc client demo

# Usage
Open SimpleRpcServer project with Android Studio and simplerpcclient project with GoLand. IDEs will download dependencies automatically

## Android
Make sure to specify the version of protoc-gen-grpc-java in `build.gradle` if you are using macOS M1

You can find all the versions and platforms of protoc-gen-grpc-java on https://repo.maven.apache.org/maven2/io/grpc/protoc-gen-grpc-java/1.42.0

```shell
protobuf {
    protoc {
        artifact = 'com.google.protobuf:protoc:3.17.3'
    }
    plugins {
        grpc {
            // https://repo.maven.apache.org/maven2/io/grpc/protoc-gen-grpc-java/1.42.0/
            // for macOS M1, using 'osx-x86_64' version
            artifact = 'io.grpc:protoc-gen-grpc-java:1.42.0:osx-x86_64'
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option "lite"
                } 
            }
            task.plugins {
                grpc { option 'lite' }
            }
        }
    }
}
```

## Golang

Add forward rule by ADB command
```shell
adb forward tcp:6666 tcp:7777
adb forward --list
```

```shell
# Generate gRPC code
protoc --go_out=plugins=grpc:. proto/FileService.proto
# Run client demo
go run main.go
```
