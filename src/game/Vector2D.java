package game;

/**
 * A 2D vector class for position, velocity, etc.
 */
public class Vector2D {
    private double x;
    private double y;
    
    /**
     * Creates a new vector with the specified components.
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Creates a new zero vector.
     */
    public Vector2D() {
        this(0, 0);
    }
    
    /**
     * Creates a copy of another vector.
     */
    public Vector2D(Vector2D other) {
        this(other.x, other.y);
    }
    
    // Getters and setters
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Sets both components at once.
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Sets this vector's components to match another vector.
     */
    public void set(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    // Vector operations
    
    /**
     * Adds another vector to this one.
     * 
     * @param other The vector to add.
     * @return This vector for chaining.
     */
    public Vector2D add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }
    
    /**
     * Adds components to this vector.
     * 
     * @param x The x component to add.
     * @param y The y component to add.
     * @return This vector for chaining.
     */
    public Vector2D add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    /**
     * Creates a new vector that is the sum of this vector and another.
     * 
     * @param other The vector to add.
     * @return A new vector.
     */
    public Vector2D plus(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }
    
    /**
     * Creates a new vector that is the sum of this vector and the given components.
     * 
     * @param x The x component to add.
     * @param y The y component to add.
     * @return A new vector.
     */
    public Vector2D plus(double x, double y) {
        return new Vector2D(this.x + x, this.y + y);
    }
    
    /**
     * Subtracts another vector from this one.
     * 
     * @param other The vector to subtract.
     * @return This vector for chaining.
     */
    public Vector2D subtract(Vector2D other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }
    
    /**
     * Subtracts components from this vector.
     * 
     * @param x The x component to subtract.
     * @param y The y component to subtract.
     * @return This vector for chaining.
     */
    public Vector2D subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    
    /**
     * Creates a new vector that is this vector minus another.
     * 
     * @param other The vector to subtract.
     * @return A new vector.
     */
    public Vector2D minus(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }
    
    /**
     * Creates a new vector that is this vector minus the given components.
     * 
     * @param x The x component to subtract.
     * @param y The y component to subtract.
     * @return A new vector.
     */
    public Vector2D minus(double x, double y) {
        return new Vector2D(this.x - x, this.y - y);
    }
    
    /**
     * Multiplies this vector by a scalar.
     * 
     * @param scalar The scalar.
     * @return This vector for chaining.
     */
    public Vector2D multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
    
    /**
     * Creates a new vector that is this vector multiplied by a scalar.
     * 
     * @param scalar The scalar.
     * @return A new vector.
     */
    public Vector2D times(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }
    
    /**
     * Divides this vector by a scalar.
     * 
     * @param scalar The scalar.
     * @return This vector for chaining.
     */
    public Vector2D divide(double scalar) {
        if (scalar != 0) {
            this.x /= scalar;
            this.y /= scalar;
        }
        return this;
    }
    
    /**
     * Creates a new vector that is this vector divided by a scalar.
     * 
     * @param scalar The scalar.
     * @return A new vector.
     */
    public Vector2D dividedBy(double scalar) {
        if (scalar != 0) {
            return new Vector2D(this.x / scalar, this.y / scalar);
        }
        return new Vector2D(this);
    }
    
    /**
     * Gets the length (magnitude) of this vector.
     * 
     * @return The length.
     */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * Gets the squared length of this vector.
     * This is faster than length() when comparing distances.
     * 
     * @return The squared length.
     */
    public double lengthSquared() {
        return x * x + y * y;
    }
    
    /**
     * Normalizes this vector (makes it unit length).
     * 
     * @return This vector for chaining.
     */
    public Vector2D normalize() {
        double len = length();
        if (len > 0) {
            x /= len;
            y /= len;
        }
        return this;
    }
    
    /**
     * Creates a new vector that is a normalized version of this one.
     * 
     * @return A new normalized vector.
     */
    public Vector2D normalized() {
        double len = length();
        if (len > 0) {
            return new Vector2D(x / len, y / len);
        }
        return new Vector2D(0, 0);
    }
    
    /**
     * Calculates the dot product of this vector and another.
     * 
     * @param other The other vector.
     * @return The dot product.
     */
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    /**
     * Calculates the cross product of this vector and another.
     * For 2D vectors, this returns a scalar (the z component of the 3D cross product).
     * 
     * @param other The other vector.
     * @return The cross product.
     */
    public double cross(Vector2D other) {
        return x * other.y - y * other.x;
    }
    
    /**
     * Calculates the distance between this vector and another.
     * 
     * @param other The other vector.
     * @return The distance.
     */
    public double distance(Vector2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculates the squared distance between this vector and another.
     * This is faster than distance() when comparing distances.
     * 
     * @param other The other vector.
     * @return The squared distance.
     */
    public double distanceSquared(Vector2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }
    
    /**
     * Linearly interpolates between this vector and another.
     * 
     * @param other The other vector.
     * @param t The interpolation parameter (0-1).
     * @return This vector for chaining.
     */
    public Vector2D lerp(Vector2D other, double t) {
        t = Math.max(0, Math.min(1, t));
        this.x = this.x + (other.x - this.x) * t;
        this.y = this.y + (other.y - this.y) * t;
        return this;
    }
    
    /**
     * Creates a new vector that is a linear interpolation between this vector and another.
     * 
     * @param other The other vector.
     * @param t The interpolation parameter (0-1).
     * @return A new vector.
     */
    public Vector2D lerpTo(Vector2D other, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Vector2D(
            this.x + (other.x - this.x) * t,
            this.y + (other.y - this.y) * t
        );
    }
    
    /**
     * Creates a new vector that is a copy of this one.
     * 
     * @return A new vector with the same components.
     */
    public Vector2D copy() {
        return new Vector2D(x, y);
    }
    
    /**
     * Rotates this vector by the given angle in radians.
     * 
     * @param angle The angle in radians.
     * @return This vector for chaining.
     */
    public Vector2D rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }
    
    /**
     * Creates a new vector that is this vector rotated by the given angle in radians.
     * 
     * @param angle The angle in radians.
     * @return A new rotated vector.
     */
    public Vector2D rotated(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector2D(
            x * cos - y * sin,
            x * sin + y * cos
        );
    }
    
    /**
     * Negates this vector.
     * 
     * @return This vector for chaining.
     */
    public Vector2D negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }
    
    /**
     * Creates a new vector that is the negation of this one.
     * 
     * @return A new negated vector.
     */
    public Vector2D negated() {
        return new Vector2D(-this.x, -this.y);
    }
    
    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Vector2D other = (Vector2D) obj;
        return Double.compare(other.x, x) == 0 && Double.compare(other.y, y) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        return result;
    }
}