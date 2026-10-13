package com.thedeathlycow.novoatlas.impl.math;

import net.minecraft.util.Mth;

public final class Blending {


    /// Hermite Spline interpolation for better blending than simple lerp.
    ///
    /// Implementation is from [the Book of Shaders](https://thebookofshaders.com/glossary/?search=smoothstep).
    ///
    /// @param edge0 Lower edge
    /// @param edge1 Upper edge
    /// @param x     Source value to interpolate
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private Blending() {

    }
}