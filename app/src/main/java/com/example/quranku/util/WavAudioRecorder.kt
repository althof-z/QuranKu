package com.example.quranku.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.*

class WavAudioRecorder(
    private val outputFile: String,
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    @Volatile private var amplitudeListener: ((Int) -> Unit)? = null

    fun startRecording() {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            writeAudioDataToFile(bufferSize)
        }
        recordingThread?.start()
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
        // After recording raw, convert to WAV
        rawToWav()
    }

    private fun writeAudioDataToFile(bufferSize: Int) {
        val data = ByteArray(bufferSize)
        val rawFile = File(outputFile + ".raw")
        FileOutputStream(rawFile).use { os ->
            while (isRecording) {
                val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                if (read > 0) {
                    os.write(data, 0, read)
                    // Compute peak amplitude from PCM 16-bit LE
                    var peak = 0
                    var i = 0
                    while (i + 1 < read) {
                        val lo = data[i].toInt() and 0xFF
                        val hi = data[i + 1].toInt()
                        val sample = (hi shl 8) or lo
                        val abs = kotlin.math.abs(sample)
                        if (abs > peak) peak = abs
                        i += 2
                    }
                    amplitudeListener?.invoke(peak)
                }
            }
        }
    }

    private fun rawToWav() {
        val rawFile = File(outputFile + ".raw")
        val wavFile = File(outputFile)
        val rawData = rawFile.readBytes()
        val totalAudioLen = rawData.size
        val totalDataLen = totalAudioLen + 36
        val channels = if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val byteRate = 16 * sampleRate * channels / 8

        val out = FileOutputStream(wavFile)
        val header = ByteArray(44)

        // RIFF/WAVE header
        header[0] = 'R'.toByte(); header[1] = 'I'.toByte(); header[2] = 'F'.toByte(); header[3] = 'F'.toByte()
        writeInt(header, 4, totalDataLen)
        header[8] = 'W'.toByte(); header[9] = 'A'.toByte(); header[10] = 'V'.toByte(); header[11] = 'E'.toByte()
        header[12] = 'f'.toByte(); header[13] = 'm'.toByte(); header[14] = 't'.toByte(); header[15] = ' '.toByte()
        writeInt(header, 16, 16) // Subchunk1Size
        writeShort(header, 20, 1.toShort()) // AudioFormat PCM
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * 16 / 8).toShort()) // BlockAlign
        writeShort(header, 34, 16.toShort()) // BitsPerSample
        header[36] = 'd'.toByte(); header[37] = 'a'.toByte(); header[38] = 't'.toByte(); header[39] = 'a'.toByte()
        writeInt(header, 40, totalAudioLen)

        out.write(header, 0, 44)
        out.write(rawData)
        out.close()
        rawFile.delete()
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = ((value shr 8) and 0xff).toByte()
        header[offset + 2] = ((value shr 16) and 0xff).toByte()
        header[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }

    fun setOnAmplitudeListener(listener: ((Int) -> Unit)?) {
        amplitudeListener = listener
    }
} 