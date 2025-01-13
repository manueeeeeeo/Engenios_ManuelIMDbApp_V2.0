package com.clase.engenios_manuelimdbapp_v20.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Manuel
 * @version 1.0*/

public class SpinnerGeneroAdapter extends ArrayAdapter<String> {
    // Variable de tipo array para guardar todas las posibles opciones del spinner
    private final String[] options;

    /**
     * @param context
     * @param options
     * @param resource
     * Constructor con el contexto, el resource y la lista de opciones*/
    public SpinnerGeneroAdapter(@NonNull Context context, int resource, @NonNull String[] options) {
        super(context, resource, options);
        this.options = options;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent) {
        // Variable para manejar los TextView
        TextView textView;
        if (convertView == null) { // En caso de que la vista no sea nula
            // Creamos un nuevo Textview
            textView = new TextView(getContext());
            // Establecemos el padding del Textview que el 16 en las 4 direcciones
            textView.setPadding(16, 16, 16, 16);
            // Establecemos el tamaño del Textview
            textView.setTextSize(16);
            // Establecemos el color del texto
            textView.setTextColor(Color.BLACK);
            // Establecemos el color de fondo del TextView
            textView.setBackgroundColor(Color.WHITE);
        } else { // En caso de que la vista sea nula
            // Obtenemos el Textview que ya existe
            textView = (TextView) convertView;
        }

        // Establecemos como texto la posición que toque dentro del array de opciones
        textView.setText(options[position]);
        // Devuelvo el textView ya formateado
        return textView;
    }
}