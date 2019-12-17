package ch.heigvd.iict.sym_labo4.gl;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import ch.heigvd.iict.sym_labo4.gl.objects.ObjLoader;

public class OpenGLRenderer implements Renderer {

    private static final String TAG = OpenGLRenderer.class.getSimpleName();

    //60 frame per second
    private static long frameInterval = Math.round(1000f / 60f);

    private ObjLoader arrow3DModel = null;

    // identity rotation matrix
    private float[] rotMatrix = {   1f, 0f ,0f ,0f,
                                    0f, 1f ,0f ,0f,
                                    0f, 0f ,1f ,0f,
                                    0f, 0f ,0f ,1f };

	public OpenGLRenderer(Context ctx) {
        try {
            InputStream in = ctx.getAssets().open("arrow.obj");
            this.arrow3DModel = new ObjLoader(in);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition
	 * .khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Set the background color to black ( rgba ).
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		// Enable Smooth Shading, default not really needed.
		gl.glShadeModel(GL10.GL_SMOOTH);
		// Depth buffer setup.
		gl.glClearDepthf(1.0f);
		// Enables depth testing.
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// The type of depth testing to do.
		gl.glDepthFunc(GL10.GL_LEQUAL);
		// Really nice perspective calculations.
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.
	 * khronos.opengles.GL10)
	 */

	public void onDrawFrame(GL10 gl) {
        // get the time at the start of the frame
        long time = System.currentTimeMillis();

		// Clears the screen and depth buffer.
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// Replace the current matrix with the identity matrix
		gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0f, 0f, 50f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        //we save current matrix stack
		gl.glPushMatrix();
            //we apply rotation
            gl.glMultMatrixf(rotMatrix, 0);
            //we draw the arrow
			this.arrow3DModel.draw(gl);
        //we restore matric stack
		gl.glPopMatrix();

        // get the time taken to render the frame
        long time2 = System.currentTimeMillis() - time;

        // if time elapsed is less than the frame interval
        if(time2 < frameInterval){
            try {
                // sleep the thread for the remaining time until the interval has elapsed
                Thread.sleep(frameInterval - time2);
            } catch (InterruptedException e) { /* InterruptedException */}
        } else {
            Log.w(TAG, "Slower than 60 fps");
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition
	 * .khronos.opengles.GL10, int, int)
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// Sets the current view port to the new size.
		gl.glViewport(0, 0, width, height);
		// Select the projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		// Reset the projection matrix
		gl.glLoadIdentity();
		// Calculate the aspect ratio of the window
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
		// Select the modelview matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// Reset the modelview matrix
		gl.glLoadIdentity();
	}

    /**
     * Method used to replace the current rotation matrix with a new one
     * @param rotMatrix The new rotationMatrix
     * @return the old rotation matrix to be recycled
     */
    public float[] swapRotMatrix(float[] rotMatrix) {
        float[] tmp = this.rotMatrix;
        this.rotMatrix = rotMatrix;
        return tmp;
    }

}
