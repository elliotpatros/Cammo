package com.emp.cammo;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.calib3d.Calib3d.CALIB_CB_ADAPTIVE_THRESH;
import static org.opencv.calib3d.Calib3d.CALIB_CB_FAST_CHECK;
import static org.opencv.calib3d.Calib3d.CALIB_CB_NORMALIZE_IMAGE;
import static org.opencv.calib3d.Calib3d.drawChessboardCorners;
import static org.opencv.calib3d.Calib3d.findChessboardCorners;
import static org.opencv.imgproc.Imgproc.drawContours;

public class Calibrator {
    private MatOfPoint2f _corners;
    private MatOfPoint2f _approxCurve;
    private Size _boardSize;

    public Calibrator(int width, int height) {
        _corners = new MatOfPoint2f();
        _approxCurve = new MatOfPoint2f();
        _boardSize = new Size(5, 4);
    }

    public void release() {
    }

    // find checkerboard
    public void findBoard(Mat src) {
//        Imgproc.cvtColor(src, _grayImage, Imgproc.COLOR_BGR2GRAY);

        boolean found = findChessboardCorners(src, _boardSize, _corners, CALIB_CB_FAST_CHECK);

        if (found) {
            double approxDistance = Imgproc.arcLength(_corners, true) * 0.02;
            Imgproc.approxPolyDP(_corners, _approxCurve, approxDistance, true);

            MatOfPoint points = new MatOfPoint(_approxCurve.toArray());

            Rect rect = Imgproc.boundingRect(points);
            Point topL = new Point(rect.x, rect.y);
            Point botR = new Point(rect.x + rect.width, rect.y + rect.height);

            Imgproc.rectangle(src, topL, botR, new Scalar(0));

            drawChessboardCorners(src, _boardSize, _corners, found);
        }
    }
}
