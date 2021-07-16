package ru.timeconqueror.lootgames.utils.future;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Vector3i implements Comparable<Vector3i> {
   /**
    * An immutable vector with zero as all coordinates.
    */
   public static final Vector3i ZERO = new Vector3i(0, 0, 0);
   private int x;
   private int y;
   private int z;

   public Vector3i(int xIn, int yIn, int zIn) {
      this.x = xIn;
      this.y = yIn;
      this.z = zIn;
   }

   public Vector3i(double xIn, double yIn, double zIn) {
      this(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Vector3i)) {
         return false;
      } else {
         Vector3i vector3i = (Vector3i) p_equals_1_;
         if (this.getX() != vector3i.getX()) {
            return false;
         } else if (this.getY() != vector3i.getY()) {
            return false;
         } else {
            return this.getZ() == vector3i.getZ();
         }
      }
   }

   public int hashCode() {
      return (this.getY() + this.getZ() * 31) * 31 + this.getX();
   }

   public int compareTo(Vector3i p_compareTo_1_) {
      if (this.getY() == p_compareTo_1_.getY()) {
         return this.getZ() == p_compareTo_1_.getZ() ? this.getX() - p_compareTo_1_.getX() : this.getZ() - p_compareTo_1_.getZ();
      } else {
         return this.getY() - p_compareTo_1_.getY();
      }
   }

   /**
    * Gets the X coordinate.
    */
   public int getX() {
      return this.x;
   }

   /**
    * Gets the Y coordinate.
    */
   public int getY() {
      return this.y;
   }

   /**
    * Gets the Z coordinate.
    */
   public int getZ() {
      return this.z;
   }

   /**
    * Sets the X coordinate.
    */
   protected void setX(int xIn) {
      this.x = xIn;
   }

   protected void setY(int yIn) {
      this.y = yIn;
   }

   /**
    * Sets the Z coordinate.
    */
   protected void setZ(int zIn) {
      this.z = zIn;
   }

   /**
    * Offset this BlockPos 1 block up
    */
   public Vector3i above() {
      return this.above(1);
   }

   /**
    * Offset this BlockPos n blocks up
    */
   public Vector3i above(int n) {
      return this.relative(EnumFacing.UP, n);
   }

   /**
    * Offset this BlockPos 1 block down
    */
   public Vector3i below() {
      return this.below(1);
   }

   /**
    * Offset this BlockPos n blocks down
    */
   public Vector3i below(int n) {
      return this.relative(EnumFacing.DOWN, n);
   }

   /**
    * Offsets this BlockPos n blocks in the given direction
    */
   public Vector3i relative(EnumFacing facing, int n) {
      return n == 0 ? this : new Vector3i(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ() + facing.getFrontOffsetZ() * n);
   }

   /**
    * Calculate the cross product of this and the given Vector
    */
   public Vector3i cross(Vector3i vec) {
      return new Vector3i(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
   }

   public boolean closerThan(Vector3i vector, double distance) {
      return this.distSqr(vector.getX(), vector.getY(), vector.getZ(), false) < distance * distance;
   }

   /**
    * Calculate squared distance to the given Vector
    */
   public double distSqr(Vector3i to) {
      return this.distSqr(to.getX(), to.getY(), to.getZ(), true);
   }

   public double distSqr(double x, double y, double z, boolean useCenter) {
      double d0 = useCenter ? 0.5D : 0.0D;
      double d1 = (double) this.getX() + d0 - x;
      double d2 = (double) this.getY() + d0 - y;
      double d3 = (double) this.getZ() + d0 - z;
      return d1 * d1 + d2 * d2 + d3 * d3;
   }

   public int distManhattan(Vector3i vector) {
      float f = (float) Math.abs(vector.getX() - this.getX());
      float f1 = (float) Math.abs(vector.getY() - this.getY());
      float f2 = (float) Math.abs(vector.getZ() - this.getZ());
      return (int) (f + f1 + f2);
   }

   public String toString() {
      return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
   }
}
