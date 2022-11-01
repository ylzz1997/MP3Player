#include "Music.h"
#include <string>
#include <iostream>
#include <vector>
#include <stdio.h>
extern "C"
{
#include <libavformat/avformat.h>
#include <libswresample/swresample.h>
}


std::string jstring2str(JNIEnv* env, jstring jstr)
{

    char* rtn = NULL;
    jclass   clsstring = env->FindClass("java/lang/String");
    jstring   strencode = env->NewStringUTF("GB2312");
    jmethodID   mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray   barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize   alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    std::string stemp(rtn);
    free(rtn);
    return stemp;
}

JNIEXPORT jint JNICALL Java_Music_load_1jni(JNIEnv* env, jobject obj, jstring js, jobject mf) {
    std::string filePath = jstring2str(env, js);
    jclass musicFormatClass = env->FindClass("MusicFormat");
    jfieldID nBytesID = env->GetFieldID(musicFormatClass, "nBytes", "I");
    jfieldID nSamplesPerSecID = env->GetFieldID(musicFormatClass, "nSamplesPerSec", "I");
    jfieldID nChannelsID = env->GetFieldID(musicFormatClass, "nChannels", "I");
    jfieldID bytesPerSampleID = env->GetFieldID(musicFormatClass, "bytesPerSample", "I");
    jfieldID musicBytesArrayID = env->GetFieldID(musicFormatClass, "musicBytesArray", "[B");

    std::vector<uint8_t> buffer;
    AVFormatContext* pFormatContext = avformat_alloc_context();
    int ret = avformat_open_input(&pFormatContext, filePath.c_str(), NULL, NULL);
    if (ret != 0) {
        return -1;
    }
    //av_dump_format(pFormatContext, 0, filePath.c_str(), 0);
    ret = avformat_find_stream_info(pFormatContext, NULL);
    if (ret < 0) {
        return -2;
    }
    int streamIndex = 0;
    for (int i = 0; i < pFormatContext->nb_streams; ++i) {
        enum AVMediaType avMediaType = pFormatContext->streams[i]->codecpar->codec_type;
        if (avMediaType == AVMEDIA_TYPE_AUDIO) {
            streamIndex = i;
        }
    }
    AVCodecParameters* avCodecParameters = pFormatContext->streams[streamIndex]->codecpar;
    enum AVCodecID avCodecId = avCodecParameters->codec_id;
    AVCodec* avCodec = avcodec_find_decoder(avCodecId);
    AVCodecContext* avCodecContext = avcodec_alloc_context3(avCodec);
    if (avCodecContext == NULL) {
        return -3;
    }
    avCodecContext->pkt_timebase = pFormatContext->streams[streamIndex]->time_base;
    avcodec_parameters_to_context(avCodecContext, avCodecParameters);
    ret = avcodec_open2(avCodecContext, avCodec, NULL);
    if (ret < 0) {
        return -4;
    }
    AVPacket* packet = (AVPacket*)av_malloc(sizeof(AVPacket));
    AVFrame* inFrame = av_frame_alloc();
    SwrContext* swrContext = swr_alloc();
    AVSampleFormat inFormat = avCodecContext->sample_fmt;
    AVSampleFormat outFormat = AV_SAMPLE_FMT_S16;
    uint64_t in_ch_layout = avCodecContext->channel_layout;
    uint64_t out_ch_layout = in_ch_layout;
    int inSampleRate = avCodecContext->sample_rate;
    //cout << inSampleRate << endl;
    int outSampleRate = inSampleRate;
    env->SetIntField(mf, nSamplesPerSecID, outSampleRate);
    env->SetIntField(mf, bytesPerSampleID, 16);
    swr_alloc_set_opts(swrContext, out_ch_layout, outFormat, outSampleRate,
        in_ch_layout, inFormat, inSampleRate, 0, NULL
    );
    swr_init(swrContext);
    int outChannelCount = av_get_channel_layout_nb_channels(out_ch_layout);
    env->SetIntField(mf, nChannelsID, outChannelCount);
    uint8_t* out_buffer = (uint8_t*)av_malloc(2 * outSampleRate);

    while (av_read_frame(pFormatContext, packet) >= 0) {
        if (packet->stream_index == streamIndex) {
            avcodec_send_packet(avCodecContext, packet);
            ret = avcodec_receive_frame(avCodecContext, inFrame);
            if (ret == 0) {
                swr_convert(swrContext, &out_buffer, 2 * outSampleRate, (const uint8_t**)inFrame->data, inFrame->nb_samples);
                int out_buffer_size = av_samples_get_buffer_size(NULL, outChannelCount, inFrame->nb_samples, outFormat, 1);
                int nowPoint = buffer.size();
                buffer.resize(buffer.size() + out_buffer_size);
                memcpy(&buffer[nowPoint], out_buffer, out_buffer_size);
            }
        }
    }
    env->SetIntField(mf, nBytesID, buffer.size());
    jbyteArray output = env->NewByteArray(buffer.size());
    // jbyte* jOutStartPoint = env->GetByteArrayElements(output,0);
    // memcpy(jOutStartPoint, &buffer[0], buffer.size());
    env->SetByteArrayRegion(output,0, buffer.size(),(jbyte *)&buffer[0]);
    env->SetObjectField(mf, musicBytesArrayID,output);
    av_frame_free(&inFrame);
    av_free(out_buffer);
    swr_free(&swrContext);
    avcodec_close(avCodecContext);
    avformat_close_input(&pFormatContext);
	return 0;
}
