网络上提供的android-library工程通过gradle打出jar的方法主要有
1、
task releaseJar(type:Jar) {  
    from sourceSet.main.java  
    destinationDir = file('build/libs')  
}  
2、
task releaseJar(type: Copy) {  
    from( 'build/bundles/release')  
    into( 'build/libs')  
    include('classes.jar')  
    rename('classes.jar', 'superlog' + VERSION_NAME + '.jar')  
}  
  
task releaseLib(type: Copy, dependsOn: releaseJar) {  
    into "../../release"  
    from 'libs'  
    from 'build/libs'  
}  

第一种可以打包出jar文件，但是这个jar文件中不会包含，工程依赖的jar,而第二种发现gradle构建后build/bundles/release
目录下不存在jar文件，所以无法验证时候可用。

最后通过google发现有一个fatjar打包的方法，具体的在build.gradle文件的实现为
build.doLast {
    releaseJar.execute()
}

task releaseJar(type:Jar) {
    baseName = 'packagejar-gradle'
    version =  '0.0.1'
    from {configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
    destinationDir = file(buildDir.absolutePath + '/outputs/')
}
参考文档：http://www.mzan.com/article/25405006-gradle-how-pack-dependencies-into-jar.shtml
对于本仓库的两个demo工程一个是在java插件中实现，一个是在android-library插件中实现的。
对于java插件那个工程不能通过idea直接编译，要进入app目录底下通过gradle build编译