package co.koko.babyfaceai.data

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Size
import androidx.annotation.WorkerThread
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

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
        val options = Interpreter.Options()
        // 필요에 따라 numThreads, NNAPI/GPU delegate 설정 가능
        interpreter = Interpreter(model, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]

        labels = FileUtil.loadLabels(context, labelName)
    }

    // 이미지를 분석하는 메인 함수
    @WorkerThread
    fun classify(bitmap: Bitmap): List<ClassificationResult> {
        // [수정] 1. 입력받은 Bitmap을 ARGB_8888 형식으로 변환하여 호환성 문제를 해결합니다.
        val argbBitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        // 2. 이미지를 TFLite가 이해할 수 있는 Float32 형태의 TensorImage로 변환합니다.
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(argbBitmap)

        // 3. 이미지를 모델의 입력 크기에 맞게 전처리합니다.
        //    [개선] TM 웹캠과 동일한 전처리 파이프라인을 구축합니다.
        val imageProcessor = ImageProcessor.Builder()
            // 센터 크롭 후 리사이즈: TM 웹캠과 유사한 전처리
            .add(ResizeWithCropOrPadOp(min(inputImageWidth, inputImageHeight), min(inputImageWidth, inputImageHeight)))
            // BILINEAR 보간법으로 정확한 크기 리사이즈
            .add(ResizeOp(inputImageWidth, inputImageHeight, ResizeMethod.BILINEAR))
            // Float 모델에 필요한 정규화: [0..255] -> [0..1]
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        val processedImage = imageProcessor.process(tensorImage)

        // 4. 모델의 출력 형태에 맞는 버퍼를 생성합니다.
        val outputShape = interpreter.getOutputTensor(0).shape()
        val probabilityBuffer = TensorBuffer.createFixedSize(outputShape, interpreter.getOutputTensor(0).dataType())

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

    // TFLite Support 라이브러리에 의존하지 않고 YUV → RGB 변환을 수행하는 헬퍼 함수
    // CameraX의 ImageProxy를 사용하는 경우, 해당 유틸리티를 사용하는 것이 가장 안정적입니다.
    fun yuv420ToRgb(image: Image): Bitmap {
        // [개선] 신뢰성 있는 YUV → RGB 변환 로직
        val yuvBytes = ByteBuffer.allocateDirect(image.width * image.height * 3 / 2)
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()
        yuvBytes.put(yBuffer)
        yuvBytes.put(vBuffer)
        yuvBytes.put(uBuffer)

        // yuvBytes를 Bitmap으로 변환하는 로직 (예: CameraX ImageAnalysis의 유틸리티 사용)
        // 이 부분은 프로젝트의 CameraX 또는 기타 표준 유틸리티에 의존하므로,
        // 해당 유틸리티를 호출하는 코드로 대체해야 합니다.
        // 예시: return image.toBitmap() (CameraX 확장 함수)

        // 이 부분은 임시 반환 값이며, 실제 구현이 필요합니다.
        val placeholderBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        return placeholderBitmap
    }
}