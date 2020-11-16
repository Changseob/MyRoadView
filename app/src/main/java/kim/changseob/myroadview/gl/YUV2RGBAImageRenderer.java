package kim.changseob.myroadview.gl;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class YUV2RGBAImageRenderer {
    private static final String TAG = "YUV2RGBAImageRenderer";
    /*
    *  Y1  Y2  Y3  Y4  Y5  Y6
    *  Y7  Y8  Y9 Y10 Y11 Y12
    * Y13 Y14 Y15 Y16 Y17 Y18
    * Y19 Y20 Y21 Y22 Y23 Y24
    *  U1  U2  U3  U4  U5  U6
    *  V1  V2  V3  V4  V5  V6
    *
    * Y to luminance of input texture
    * UV to luminance-alpha of input texture
    * */


    // vertex shader
    private final String mVertShader =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexcoord;\n" +
            "uniform mat4 view;\n" +
            "varying vec2 vTexcoord;\n" +
            "void main()\n" +
            "{\n" +
            "   gl_Position = view * aPosition;\n" +
            "   vTexcoord = aTexcoord;\n" +
            "}\n";

    // fragment shader (YUV to RGBA
    private final String mFragShader =
            "precision mediump float;\n" +
            "uniform sampler2D yTex;\n" +
            "uniform sampler2D uvTex;\n" +
            "\n" +
            "const lowp float one = 1.0;\n" +
            "uniform float alpha;\n"+
            "const lowp float zero = 0.0;\n" +
            "varying vec2 vTexcoord;\n" +
            "void main(void) \n" +
            "{\n" +
                "\tconst lowp vec3 delta = vec3(zero, -0.5, -0.5);\n" +
                "\tconst lowp mat3 conv = mat3(\n" +
                "\t\tone,\t one, \tone,  \n" +
                "\t \t1.402, -0.714,  zero, \n" +
                "\t \tzero, -0.344, 1.772);\n" +
                "\n" +
                "\tlowp vec3 yuv = vec3(texture2D(yTex,vTexcoord).r, texture2D(uvTex,\n" +
                "\t\tvTexcoord).ra);\n" +
                "\tyuv = yuv + delta;\n" +
                "\tlowp vec3 rgb = conv * yuv;\n" +
                "\t\n" +
                "\tgl_FragData[0]=vec4(rgb, alpha);\n" +
            "}\n";

    protected int mProgram;
    // Y Texture, UV Texture
    protected int[] mSrcTex = new int[2];

    protected float[] imagePlaneVertices = {
            -1.0f, -1.0f, -1.0f,    // left bottom
            1.0f, -1.0f, -1.0f,     // right bottom
            1.0f, 1.0f, -1.0f,      // right top
            -1.0f, 1.0f, -1.0f};    // left top

    protected float[] imagePlaneTexCoord = {
            0.0f, 1.0f,     // left bottom
            1.0f, 1.0f,     // right bottom
            1.0f, 0.0f,     // right top
            0.0f, 0.0f};    // left top


    // image plane vertices buffer, image plane texture coordinates buffer
    protected FloatBuffer ipvBuffer, iptBuffer;

    protected byte imagePlaneIndices[] = {0, 1, 2, 2, 3, 0};

    public YUV2RGBAImageRenderer() {

        /* initialize renderer */

        ipvBuffer = Utils.MakeFloatBuffer(imagePlaneVertices);
        iptBuffer = Utils.MakeFloatBuffer(imagePlaneTexCoord);

        mSrcTex[0] = 0;
        mSrcTex[1] = 0;

        // create program and compile&load shader
        mProgram = GLES20.glCreateProgram();
        int vertexShader = Utils.LoadShader(GLES20.GL_VERTEX_SHADER, mVertShader);
        if(vertexShader == 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }

        int fragmentShader = Utils.LoadShader(GLES20.GL_FRAGMENT_SHADER, mFragShader);
        if(fragmentShader == 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }

        if(mProgram != 0) {
            GLES20.glAttachShader(mProgram, vertexShader);
            Utils.CheckGLError("glAttachShader.vertexShader");

            GLES20.glAttachShader(mProgram, fragmentShader);
            Utils.CheckGLError("glAttachShader.fragmentShader");

            GLES20.glLinkProgram(mProgram);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if(linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(mProgram));
                GLES20.glDeleteProgram(mProgram);
                mProgram = 0;
            }
        }
    }

    public void SetImageData(int width, int height, byte[] data) {
        if(mSrcTex[0] == 0) {
            GLES20.glGenTextures(2, mSrcTex, 0);
            for(int i = 0; i < 2; i++) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSrcTex[i]);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }
            ByteBuffer bb = ByteBuffer.wrap(data);
            byte[] y = new byte[width * height];
            bb.get(y);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSrcTex[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(y));

            ByteBuffer bbuv = bb.slice();
            byte[] uv = new byte[width * height >> 1];
            bbuv.get(uv);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, width >>1, height>>1, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(uv));
        } else {
            ByteBuffer bb = ByteBuffer.wrap(data);
            byte[] y = new byte[width * height];
            bb.get(y);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSrcTex[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(y));

            ByteBuffer bbuv = bb.slice();
            byte[] uv = new byte[width * height >> 1];
            bbuv.get(uv);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width >> 1, height >> 1, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(uv));
        }
    }

    public void Draw(float alpha, float[] view) {
        int loc;
        GLES20.glUseProgram(mProgram);

        // set textures
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSrcTex[0]);
        loc = GLES20.glGetUniformLocation(mProgram, "yTex");
        GLES20.glUniform1i(loc, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSrcTex[1]);
        loc = GLES20.glGetUniformLocation(mProgram, "uvTex");
        GLES20.glUniform1i(loc, 1);

        // set texture coordinates
        loc = GLES20.glGetAttribLocation(mProgram, "aTexcoord");
        GLES20.glVertexAttribPointer(loc, 2, GLES20.GL_FLOAT, false, 0, iptBuffer);
        GLES20.glEnableVertexAttribArray(loc);

        // set vertices
        loc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glVertexAttribPointer(loc, 3, GLES20.GL_FLOAT, false, 0, ipvBuffer);
        GLES20.glEnableVertexAttribArray(loc);

        // set alpha(transparency)
        loc = GLES20.glGetUniformLocation(mProgram, "alpha");
        GLES20.glUniform1f(loc, alpha);

        // set view matrix
        loc = GLES20.glGetUniformLocation(mProgram, "view");
        GLES20.glUniformMatrix4fv(loc, 1, false, view, 0);

        // draw from indices
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(imagePlaneIndices));
        GLES20.glFlush();
    }

    @Override
    protected void finalize() throws Throwable {
        GLES20.glDeleteTextures(2, mSrcTex, 0);
        super.finalize();
    }
}
