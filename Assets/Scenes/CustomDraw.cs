using System.Collections;
using System.Collections.Generic;

using UnityEngine;

public class CustomDraw : MonoBehaviour
{
    Vector2 mousePresPos = Vector2.zero;
    Vector2 lastMousePos = Vector2.zero;
    private float yAccumulatedInput;
    private float xAccumulatedInput;
    static public Vector2 mousePosition = Vector2.zero;

    public void updateMousePos()
    {
        Vector2 mousePos = Input.mousePosition;

        if (mousePos != mousePresPos)
        {
            mousePresPos = mousePos;
            lastMousePos = mousePos;
            yAccumulatedInput = 0;
            xAccumulatedInput = 0;
        }
        else
        {
            var mx = Input.GetAxis("Mouse Y");
            var my = Input.GetAxis("Mouse X");
            if (mx != 0 || my != 0)
            {
                xAccumulatedInput += mx;
                yAccumulatedInput += my;
                lastMousePos.x = mousePos.x + (yAccumulatedInput * 15f);
                lastMousePos.y = mousePos.y + (xAccumulatedInput * 15f);
            }
        }
        mousePosition = lastMousePos;
    }


    void Update()
    {
        updateMousePos();
        Debug.Log("Mouse position: " + mousePosition);
    }
}
