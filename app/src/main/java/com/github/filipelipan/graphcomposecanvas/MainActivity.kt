package com.github.filipelipan.graphcomposecanvas

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.github.filipelipan.graphcomposecanvas.ui.theme.GraphComposeCanvasTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GraphComposeCanvasTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2E3D4C))
                            .fillMaxWidth()
                            .padding(top = 50.dp)
                            .height(300.dp)
                    ) {
                        Graph(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            axis = Axis(
                                axisStyle = AxisStyle(
                                    axisStrokeWidth = 5f,
                                    shadowColors = listOf(
                                        Color.Yellow.copy(alpha = 0.2f),
                                        Color.Magenta.copy(alpha = 0.1f),
                                        Color.Transparent,
                                    ),
                                    dotColor = Color.Magenta,
                                    dotRadius = 10f,
                                    lineColors = listOf(
                                        Color.Yellow,
                                        Color.Magenta
                                    ),
                                ),
                                data = listOf(
                                    GraphPoint(2022, 2),
                                    GraphPoint(2023, 3),
                                    GraphPoint(2024, 6),
                                    GraphPoint(2024, 0),
                                    GraphPoint(2025, 4),
                                ),
                            ),
                            graphStyle = GraphStyle(
                                paddingStart = 60f,
                                paddingBottom = 100f,
                                strokeWidth = 5f,
                                linesWidth = 1f
                            ),
                            maxValue = 6,
                        )
                    }
                }
            }
        }
    }
}

class GraphPoint(
    val year: Int,
    val value: Int
)

class Axis(
    val axisStyle: AxisStyle,
    val data: List<GraphPoint>,
)

data class GraphStyle(
    val paddingStart: Float,
    val paddingBottom: Float,
    val strokeWidth: Float,
    val linesWidth: Float,
)

data class AxisStyle(
    val shadowColors: List<Color>,
    val lineColors: List<Color>,
    val dotColor: Color = Color.Magenta,
    val dotRadius: Float = 10f,
    val axisStrokeWidth: Float,
)

@Composable
fun Graph(
    modifier: Modifier,
    axis: Axis,
    maxValue: Int,
    graphStyle: GraphStyle
) {
    BoxWithConstraints(modifier = modifier) {
        val height = this.maxHeight
        val width = this.maxWidth

        Canvas(modifier = modifier) {

            val heightPx = height.toPx()
            val widthPx = width.toPx()

            val bottomGraph = (heightPx - graphStyle.strokeWidth) - graphStyle.paddingBottom
            val startGraph = graphStyle.paddingStart + graphStyle.strokeWidth
            val firstColumnPosition = startGraph + 60f

            val xAxisWidth = widthPx - firstColumnPosition

            val xAxisGapDistance = xAxisWidth / axis.data.size
            val yAxisGapDistance = bottomGraph / maxValue

            val dotsOffSet: MutableList<Offset> = mutableListOf()

            dotsOffSet.add(Offset(0f, 200f))


            axis.data.forEachIndexed { index, point ->
                val dotXPosition = firstColumnPosition + (xAxisGapDistance * index)
                val dotYPosition = bottomGraph - (yAxisGapDistance * point.value)

                val dotOffset = Offset(x = dotXPosition, y = dotYPosition)

                dotsOffSet.add(dotOffset)
            }

            drawColumnText(
                dotsOffSet = dotsOffSet,
                heightPx = heightPx,
                axis = axis
            )

            drawLines(maxValue, startGraph, yAxisGapDistance, widthPx, graphStyle.linesWidth)

            drawAxis(
                bottomGraph = bottomGraph,
                canvasWidthPx = widthPx,
                pointsOffSet = dotsOffSet,
                lastOffSet = dotsOffSet.last(),
                axisStyle = axis.axisStyle
            )

            drawLineText(maxValue, yAxisGapDistance, widthPx)
        }
    }
}

private fun DrawScope.drawLineText(
    maxValue: Int,
    yAxisGapDistance: Float,
    widthPx: Float
) {
    for (i in 0..maxValue) {
        val textPath = android.graphics.Path().apply {
            moveTo(0f, yAxisGapDistance * i)
            lineTo(widthPx, yAxisGapDistance * i)
        }

        drawContext.canvas.nativeCanvas.apply {
            drawTextOnPath(
                abs((i - maxValue)).toString(),
                textPath,
                20f,
                12f,
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 36f
                    textAlign = Paint.Align.LEFT
                }
            )
        }

        drawPath(
            path = textPath.asComposePath(),
            color = Color.Transparent,
        )
    }
}

private fun DrawScope.drawLines(
    maxValue: Int,
    startGraph: Float,
    yAxisGapDistance: Float,
    widthPx: Float,
    linesWidth: Float
) {
    for (i in 0..maxValue) {
        drawLine(
            start = Offset(startGraph, yAxisGapDistance * i),
            end = Offset(widthPx, yAxisGapDistance * i),
            color = Color.Black,
            strokeWidth = linesWidth
        )
    }
}

private fun DrawScope.drawColumnText(
    dotsOffSet: MutableList<Offset>,
    heightPx: Float,
    axis: Axis
) {
    dotsOffSet
        .drop(1)
        .forEachIndexed { index, offset ->
            val textPath = android.graphics.Path().apply {
                moveTo(offset.x - 50f, heightPx)
                lineTo(offset.x + 50f, heightPx)
            }

            drawContext.canvas.nativeCanvas.apply {
                drawTextOnPath(
                    axis.data[index].year.toString(),
                    textPath,
                    0f,
                    0f,
                    Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 36f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }

            drawPath(
                path = textPath.asComposePath(),
                color = Color.Transparent,
            )
        }
}

private fun DrawScope.drawAxis(
    bottomGraph: Float,
    canvasWidthPx: Float,
    pointsOffSet: MutableList<Offset> = mutableListOf(),
    lastOffSet: Offset = Offset(canvasWidthPx, 0f),
    axisStyle: AxisStyle
) {
    val path = Path().apply { moveTo(0f, 200f) }
    var shadowPath = Path()

    val endOffSet = Offset(canvasWidthPx, 600f)
//    pointsOffSet.forEach {
//        path.lineTo(it.x, it.y)
//
//
//    }
//    drawPath(
//        path = path,
//        color = Color.Green,
//        style = Stroke(width = axisStyle.axisStrokeWidth, join = StrokeJoin.Round)
//    )

    // add curves to the path
    pointsOffSet.add(endOffSet)
    pointsOffSet
        .windowed(2)
        .forEach {
            val offset1 = it.first()
            val offset2 = it.last()

            // don't use last offset on the shadow
            if (offset2 == endOffSet) {
                shadowPath.addPath(path)
            }

            val columnHalfXPosition =
                getColumnHalfXPosition(columnStartOffset = offset1, columnEndOffset = offset2)

            // https://proandroiddev.com/drawing-bezier-curve-like-in-google-material-rally-e2b38053038c
            path.cubicTo(
                columnHalfXPosition, offset1.y,
                columnHalfXPosition, offset2.y,
                offset2.x, offset2.y
            )
        }

    drawShadow(shadowPath, lastOffSet, bottomGraph, canvasWidthPx, axisStyle)

    drawAxisLine(path, axisStyle)

    drawCircle(axisStyle, lastOffSet)
}

private fun DrawScope.drawCircle(
    axisStyle: AxisStyle,
    lastOffSet: Offset
) {
    this.drawCircle(
        axisStyle.dotColor,
        radius = axisStyle.dotRadius,
        center = lastOffSet
    )
}

private fun DrawScope.drawAxisLine(
    path: Path,
    axisStyle: AxisStyle
) {
    // Path
    val axisPath = Path().apply { addPath(path) }

    this.drawPath(
        path = axisPath,
        brush = Brush.verticalGradient(colors = axisStyle.lineColors),
        style = Stroke(width = axisStyle.axisStrokeWidth, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawShadow(
    path: Path,
    lastOffSet: Offset,
    bottomGraph: Float,
    canvasWidthPx: Float,
    axisStyle: AxisStyle
) {
    // shadow
    val shadowPath = Path().apply { addPath(path) }

    // add bottom coordinates
    shadowPath.lineTo(lastOffSet.x, bottomGraph)
    shadowPath.lineTo(0f, bottomGraph)
    shadowPath.close()

//    drawRect(
//        topLeft = Offset.Zero,
//        size = Size(canvasWidthPx, bottomGraph),
//        brush = Brush.verticalGradient(
//            colors = axisStyle.shadowColors,
//        ),
//    )
//    drawPath(shadowPath, Color.Green)

    clipPath(
        path = shadowPath,
        clipOp = ClipOp.Intersect
    ) {
        drawRect(
            topLeft = Offset.Zero,
            size = Size(canvasWidthPx, bottomGraph),
            brush = Brush.verticalGradient(
                colors = axisStyle.shadowColors,
            ),
        )
    }
}

fun getColumnHalfXPosition(columnStartOffset: Offset, columnEndOffset: Offset): Float {
    return ((columnEndOffset.x - columnStartOffset.x) / 2) + columnStartOffset.x
}