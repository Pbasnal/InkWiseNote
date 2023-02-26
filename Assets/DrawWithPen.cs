using FreeDraw;

using UnityEngine;

public class DrawWithPen : MonoBehaviour
{
    [SerializeField] private Drawable drawable;

    // PEN COLOUR
    public static Color Pen_Colour = Color.red;     // Change these to change the default drawing settings
                                                    // PEN WIDTH (actually, it's a radius, in pixels)
    public static int Pen_Width = 3;


    public delegate void Brush_Function(Vector2 world_position);
    // This is the function called when a left click happens
    // Pass in your own custom one to change the brush type
    // Set the default function in the Awake method
    public Brush_Function current_brush;

    public LayerMask Drawing_Layers;

    public bool Reset_Canvas_On_Play = true;
    // The colour the canvas is reset to each time
    public Color Reset_Colour = new Color(0, 0, 0, 0);  // By default, reset the canvas to be transparent

    // MUST HAVE READ/WRITE enabled set in the file editor of Unity
    Sprite drawable_sprite;
    Texture2D drawable_texture;

    Vector2 previous_drag_position;
    Color[] clean_colours_array;
    Color transparent;
    Color32[] cur_colors;
    bool mouse_was_previously_held_down = false;
    bool no_drawing_on_current_drag = false;

    private void Update()
    {
        bool mouse_held_down = Input.GetMouseButton(0);
        if (mouse_held_down && !no_drawing_on_current_drag)
        {
            // Convert mouse coordinates to world coordinates
            Vector2 mouse_world_position = Camera.main.ScreenToWorldPoint(Input.mousePosition);

            // Check if the current mouse position overlaps our image
            Collider2D hit = Physics2D.OverlapPoint(mouse_world_position, Drawing_Layers.value);
            if (hit != null && hit.transform != null)
            {
                // We're over the texture we're drawing on!
                // Use whatever function the current brush is
                drawable.current_brush(mouse_world_position);
            }

            else
            {
                // We're not over our destination texture
                previous_drag_position = Vector2.zero;
                if (!mouse_was_previously_held_down)
                {
                    // This is a new drag where the user is left clicking off the canvas
                    // Ensure no drawing happens until a new drag is started
                    no_drawing_on_current_drag = true;
                }
            }
        }
        // Mouse is released
        else if (!mouse_held_down)
        {
            previous_drag_position = Vector2.zero;
            no_drawing_on_current_drag = false;
        }
        mouse_was_previously_held_down = mouse_held_down;
    }
}
