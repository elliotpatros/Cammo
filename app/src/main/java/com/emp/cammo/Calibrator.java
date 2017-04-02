package com.emp.cammo;

import android.os.AsyncTask;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.opencv.calib3d.Calib3d.CALIB_CB_FAST_CHECK;
import static org.opencv.calib3d.Calib3d.CALIB_USE_LU;
import static org.opencv.calib3d.Calib3d.drawChessboardCorners;
import static org.opencv.calib3d.Calib3d.findChessboardCorners;

public class Calibrator {
    private MatOfPoint2f _corners;
    private MatOfPoint2f _approxCurve;
    private Size _boardSize;

    public Calibrator() {
        _corners = new MatOfPoint2f();
        _approxCurve = new MatOfPoint2f();
        _boardSize = new Size(5, 4);
    }

    // find checkerboard
    public Mat findBoard(Mat src) {
        Finder finder = new Finder();
        finder.execute(src);
        try {
            finder.get(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        } catch (TimeoutException e) {
            return null;
        }

        return src;
    }

    private class Finder extends AsyncTask<Mat, Void, Void> {
        @Override
        protected Void doInBackground(Mat... params) {
            boolean found = findChessboardCorners(params[0], _boardSize, _corners, CALIB_CB_FAST_CHECK + CALIB_USE_LU);

            if (found) {
                double approxDistance = Imgproc.arcLength(_corners, true) * 0.02;
                Imgproc.approxPolyDP(_corners, _approxCurve, approxDistance, true);

//            MatOfPoint points = new MatOfPoint(_approxCurve.toArray());
//
//            Rect rect = Imgproc.boundingRect(points);
//            Point topL = new Point(rect.x, rect.y);
//            Point botR = new Point(rect.x + rect.width, rect.y + rect.height);
//
//            Imgproc.rectangle(src, topL, botR, new Scalar(0), 2);

                drawChessboardCorners(params[0], _boardSize, _corners, found);
            }

            return null;
        }
    }
}
