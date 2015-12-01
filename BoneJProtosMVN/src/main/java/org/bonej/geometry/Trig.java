package org.bonej.geometry;

import javax.vecmath.Vector3d;

/**
 * Provides simple trigonometric calculations
 *
 * @author Michael Doube
 * @author <a href="mailto:rdomander@rvc.ac.uk">Richard Domander</a>
 */
public class Trig {

    /**
     * <p>
     * Calculate the distance between 2 3D points p and q using Pythagoras'
     * theorem, <i>a</i><sup>2</sup> = <i>b</i><sup>2</sup> +
     * <i>c</i><sup>2</sup>
     * </p>
     *
     * @param p
     *            a 3 element array
     * @param q
     *            another 3 element array
     * @return distance between <i>p</i> and <i>q</i>
     */
    public static double distance3D(double[] p, double[] q) {
        return distance3D(p[0], p[1], p[2], q[0], q[1], q[2]);
    }

    /**
     * <p>
     * Calculate the distance between 2 3D points <i>p</i>(x, y, z) and
     * <i>q</i>(x, y, z) using Pythagoras' theorem
     * </p>
     *
     * @param px
     *            x-coordinate of first point
     * @param py
     *            y-coordinate of first point
     * @param pz
     *            z-coordinate of first point
     * @param qx
     *            x-coordinate of second point
     * @param qy
     *            y-coordinate of second point
     * @param qz
     *            z-coordinate of second point
     * @return
     */
    public static double distance3D(double px, double py, double pz, double qx,
                                    double qy, double qz) {
        return distance3D(px - qx, py - qy, pz - qz);
    }

    /**
     * <p>
     * Calculate the distance to the origin, (0,0,0). Given 3 orthogonal
     * vectors, calculates the vector sum
     * </p>
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double distance3D(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double distance3D(double[] v) {
        return distance3D(v[0], v[1], v[2]);
    }
}
