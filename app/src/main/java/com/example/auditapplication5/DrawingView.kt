package com.example.auditapplication5

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.auditapplication5.presentation.viewmodel.AInfo5ViewModel
import kotlin.math.pow
import kotlin.math.sqrt

class DrawingView(context: Context, attrs: AttributeSet): View(context,attrs) {

    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.RED
    private var canvas : Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    private val mTextList = ArrayList<CustomText>()
    private val mUndoTextList = ArrayList<CustomText>()

    var startX = 0.0f
    var startY = 0.0f
    var endX = 0.0f
    var endY = 0.0f

    var chooseShape = 0



    private var textDrawnFlag = false
    fun setFlagIfTextDrawn(input: Boolean){
        textDrawnFlag = input
    }
    fun getFlagIfTextDrawn(): Boolean{
        return textDrawnFlag
    }

    private var flagRotationInput = 1
    fun setRotationInputFlag(input: Int){
        flagRotationInput = input
    }
    fun getRotationInputFlag(): Int{
        return flagRotationInput
    }


    private var textSize : Float = 40.0F
    fun setSizeForText(input: Float){
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            input, resources.displayMetrics)
    }
    fun getSizeForText(): Float{
        return textSize
    }

    private var textColor: Int = 0
    fun setColorForText(input: Int){
        textColor = input
    }
    fun getColorForText(): Int{
        return textColor
    }

    private var pathDirectionForText = 1
    fun setTextPathDirection(input: Int){
        pathDirectionForText = input
    }
    fun getTextPathDirection(): Int{
        return pathDirectionForText
    }

    var textPath = Path()
    fun setPathForText(input: Path){
        textPath = input
    }
    fun getPathForText(): Path {
        return textPath
    }
    //Mutable Live Data for Dynamic Inputting of Text

    var textInput = MutableLiveData<String>()



    init {
        setUpDrawing()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!, 0f,0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint?.style = Paint.Style.STROKE
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }

        if (mTextList.size > 0){
            for (texts in mTextList){
                mDrawPaint!!.textSize = texts.textSize
                mDrawPaint!!.color =  texts.textColor
                mDrawPaint?.style = Paint.Style.FILL_AND_STROKE
                mDrawPaint?.strokeWidth = 0.0F
                setPathForText(pathsForTextWriting(texts.pathDirection,texts.textStartX,texts.textStartY))
                canvas?.drawTextOnPath(texts.text, getPathForText(),0.0F,0.0F,mDrawPaint!!)
                mDrawPaint?.style = Paint.Style.STROKE
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        val textStuff = "I am going home to town"

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()

                mDrawPath!!.moveTo(touchX!!, touchY!!)
                startX = touchX
                startY = touchY

                if (chooseShape == 1){
                    canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    invalidate()
                    mDrawPaint?.textSize = getSizeForText()
                    mDrawPaint?.color = color
                    setColorForText(color)
                    mDrawPaint?.style = Paint.Style.FILL_AND_STROKE
                    mDrawPaint?.strokeWidth = 0.0F
                    canvas?.drawTextOnPath(textInput.value.toString(),
                        pathsForTextWriting(getTextPathDirection(),
                            startX,startY),
                        0.0F, 0.0F, mDrawPaint!!)
                    setFlagIfTextDrawn(true)
                } else{
                    mDrawPaint?.style = Paint.Style.STROKE
                }

            }

            MotionEvent.ACTION_MOVE -> {
                endX = touchX!!
                endY = touchY!!

                when (chooseShape) {
                    1 -> {

                    }
                    2 -> {
                        mDrawPath!!.reset()
                        mDrawPath!!.moveTo(startX,startY)
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                    3 -> {
                        mDrawPath!!.reset()
                        mDrawPath!!.moveTo(startX,startY)
                        mDrawPath!!.addRect(CustomRectangle(startX,startY,touchX,touchY), Path.Direction.CW)
                    }
                    4 -> {
                        mDrawPath!!.reset()
                        mDrawPath!!.moveTo(startX,startY)
                        mDrawPath!!.addOval(CustomRectangle(startX,startY,touchX,touchY), Path.Direction.CW)
                    }
                }

            }

            MotionEvent.ACTION_UP -> {
                endX = touchX!!
                endY = touchY!!
                when(chooseShape) {
                    1-> {
                        }
                    2 -> {
                        mDrawPath!!.moveTo(startX,startY)
                        val arrowPointsAtStart = determinePoints2And3(startX,startY, endX,endY)
                        drawTriangle(startX,startY, arrowPointsAtStart[0], arrowPointsAtStart[1],
                            arrowPointsAtStart[2],arrowPointsAtStart[3])
                        mDrawPath!!.lineTo(endX, endY)
                        val arrowPointsAtEnd = determinePoints2And3(endX,endY, startX,startY)
                        drawTriangle(endX,endY, arrowPointsAtEnd[0], arrowPointsAtEnd[1],
                            arrowPointsAtEnd[2],arrowPointsAtEnd[3])
                        mPaths.add(mDrawPath!!)
                        mDrawPath = CustomPath(color, mBrushSize)
                    }
                    3 -> {
                        mDrawPath!!.moveTo(startX,startY)
                        mDrawPath!!.addRect(CustomRectangle(startX,startY,endX,endY), Path.Direction.CW)
                        mPaths.add(mDrawPath!!)
                        mDrawPath = CustomPath(color, mBrushSize)
                    }
                    4 -> {
                        mDrawPath!!.addOval(CustomRectangle(startX,startY,endX,endY), Path.Direction.CW)
                        mPaths.add(mDrawPath!!)
                        mDrawPath = CustomPath(color, mBrushSize)
                    }
                }
            }else -> return false
        }

        invalidate()
        return true

    }


    // Functions go Below

    fun onClickUndo(){
        if(mPaths.size>0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
            invalidate()

        }
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 6.toFloat()
        chooseShape = 4
        setColorForText(color)
    }

    fun setSizeForBrush(newSize: Float){
        // Ensures that the brush size is the same for different displays.
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    fun CustomRectangle(startX: Float, startY: Float, endX: Float, endY: Float): RectF {
        return RectF(startX, startY, endX, endY)

    }

    //Arrows related functions

    fun determinePoints2And3(tipX: Float, tipY: Float, tailX: Float, tailY: Float ): MutableList<Float> {
        val twoOtherPointsArray = mutableListOf<Float>()
        val arrowHeight = mBrushSize*2.3F
        val arrowWidth = mBrushSize*2.3F

        val lineLength = sqrt((tipX-tailX).pow(2) + (tipY-tailY).pow(2))
        val deltaX = -tipX + tailX
        val deltaY = -tipY + tailY

        val p1X = tipX + (arrowHeight*deltaX + arrowWidth*deltaY/2)/lineLength
        val p1Y = tipY + (arrowHeight*deltaY - arrowWidth*deltaX/2)/lineLength

        val p2X = tipX + (arrowHeight*deltaX - arrowWidth*deltaY/2)/lineLength
        val p2Y = tipY + (arrowHeight*deltaY + arrowWidth*deltaX/2)/lineLength

        twoOtherPointsArray.add(p1X)
        twoOtherPointsArray.add(p1Y)
        twoOtherPointsArray.add(p2X)
        twoOtherPointsArray.add(p2Y)

        return twoOtherPointsArray
    }

    fun drawTriangle(x1: Float, y1: Float, x2: Float, y2: Float,x3: Float, y3: Float ){
        val pathTriangle = mDrawPath
        if (pathTriangle != null) {
            pathTriangle.moveTo(x1,y1)
        }
        if (pathTriangle != null) {
            pathTriangle.lineTo(x2,y2)
        }
        if (pathTriangle != null) {
            pathTriangle.moveTo(x3,y3)
        }
        if (pathTriangle != null) {
            pathTriangle.lineTo(x1,y1)
        }
    }


    //Text related functions
    fun onClickTextUndo(){
        if (mTextList.size > 0){
            mUndoTextList.add(mTextList.removeAt(mTextList.size -1))
        }
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }
    fun pathsForTextWriting(type: Int, startX: Float, startY: Float): Path{
        val path = Path()
        when(type){
            1 -> {
                path.moveTo(startX, startY)
                path.lineTo(startX + 10000.0F, startY)
            }
            2 -> {
                path.moveTo(startX, startY)
                path.lineTo(startX, startY+ 10000.0F)
            }

            3 -> {
                path.moveTo(startX, startY)
                path.lineTo(startX - 10000.0F, startY)
            }
            4-> {
                path.moveTo(startX, startY)
                path.lineTo(startX, startY - 10000.0F)}
        }
        return path
    }
    fun textRotate(){
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
        mDrawPaint?.strokeWidth = 0.0F
        mDrawPaint?.style = Paint.Style.FILL_AND_STROKE
        mDrawPaint?.textSize = getSizeForText()
        mDrawPaint?.color = getColorForText()
        canvas?.drawTextOnPath(textInput.value.toString(),
            pathsForTextWriting(getTextPathDirection(),startX,startY),
            0.0F, 0.0F, mDrawPaint!!)
    }
    fun textSave(){
        val customText = CustomText(textInput.value.toString(), startX,startY, getTextPathDirection(),getSizeForText(),getColorForText())
        mTextList.add(customText)
        setFlagIfTextDrawn(false)
        setTextPathDirection(1)
    }

    //Classes
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
    internal inner class CustomText(var text: String, var textStartX: Float, var textStartY: Float, var pathDirection: Int, var textSize: Float, var textColor: Int)


}