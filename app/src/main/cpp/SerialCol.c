//
// Created by root on 20-10-11.
//
#include <jni.h>

#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

int serialfd = -1;
static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 327600:
            return B500000;
        case 921600:
            return B921600;

        default:
            return -1;
    }
}


JNIEXPORT jobject JNICALL
Java_com_hikivision_Hardware_HardCol_SerialOpen(JNIEnv *env, jclass clazz, jstring path,
                                                 jint baudrate, jint flags) {
    int fd;
    speed_t speed;
    jobject mFileDescriptor;
    /* Check arguments */
    {
        speed = getBaudrate(baudrate);
        if (speed == -1) {
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf =(*env)->GetStringUTFChars(env,path, &iscopy);
        if (path_utf[strlen(path_utf) - 1] == '0') {//如果为调试串口,设置为普通串口
            fd = open("/dev/console", O_RDONLY); // 改变console
            ioctl(fd, TIOCCONS);
            close(fd);
        }
        fd = open(path_utf, O_RDWR);


        serialfd = fd;
        (*env)->ReleaseStringUTFChars(env,path, path_utf);
        if (fd == -1) {
            /* Throw an exception */
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Configure device */
    {
        struct termios cfg;
        if (tcgetattr(fd, &cfg)) {
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        if (tcsetattr(fd, TCSANOW, &cfg)) {
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }
    }

    // Create a corresponding file descriptor
    {
        jclass cFileDescriptor = (*env)->FindClass(env,"java/io/FileDescriptor");
        jfieldID descriptorID = (*env)->GetFieldID(env,cFileDescriptor, "descriptor", "I");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env,cFileDescriptor, "<init>", "()V");
        mFileDescriptor = (*env)->NewObject(env,cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env,mFileDescriptor, descriptorID, (jint) fd);
    }

    return mFileDescriptor;
}

JNIEXPORT void JNICALL
Java_com_hikivision_Hardware_HardCol_SerialClose(JNIEnv *env, jclass clazz) {
    int fd;
    if(-1!=serialfd)
        close(serialfd);
        fd = open("/dev/console",O_RDONLY); //恢复console 到串口0
        ioctl(fd,TIOCCONS);
        close(fd);
    }
