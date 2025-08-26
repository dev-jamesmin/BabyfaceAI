package co.koko.babyfaceai.data

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.Interpreter

// 분석 결과를 담을 데이터 클래스
data class ClassificationResult(val label: String, val confidence: Float)

class AgeClassifier(
    private val context: Context,
    private val modelName: String = "model.tflite",
    private val labelName: String = "labels.txt"
) {
    private val interpreter: Interpreter
    private val inputImageWidth: Int
    private val inputImageHeight: Int
    private val labels: List<String>

    init {
        val model = FileUtil.loadMappedFile(context, modelName)
        interpreter = Interpreter(model)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]

        labels = FileUtil.loadLabels(context, labelName)
    }

    // 이미지를 분석하는 메인 함수
    fun classify(bitmap: Bitmap): List<ClassificationResult> {
        // [수정] 1. 입력받은 Bitmap을 ARGB_8888 형식으로 변환하여 호환성 문제를 해결합니다.
        val argbBitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        // 2. 이미지를 TFLite가 이해할 수 있는 Float32 형태의 TensorImage로 변환합니다.
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(argbBitmap) // 변환된 비트맵을 사용합니다.

        // 3. 이미지를 모델의 입력 크기에 맞게 리사이즈하고, 정규화합니다.
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        val processedImage = imageProcessor.process(tensorImage)

        // 4. 모델의 출력 형태에 맞는 버퍼를 생성합니다.
        val probabilityBuffer = TensorBuffer.createFixedSize(interpreter.getOutputTensor(0).shape(), interpreter.getOutputTensor(0).dataType())

        // 5. 모델을 실행하여 추론합니다.
        interpreter.run(processedImage.buffer, probabilityBuffer.buffer.rewind())

        // 6. 출력된 확률을 라벨과 매칭하여 결과 리스트를 만듭니다.
        val tensorLabel = TensorLabel(labels, probabilityBuffer)
        val results = mutableListOf<ClassificationResult>()
        tensorLabel.mapWithFloatValue.forEach { (label, confidence) ->
            results.add(ClassificationResult(label, confidence))
        }

        // 확률이 높은 순서대로 정렬하여 반환합니다.
        results.sortByDescending { it.confidence }
        return results
    }
}
