
package com.example.trilateration20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ScrollView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myView = (ScrollView)findViewById(R.id.myview);
        clearHelp();
    }

    /* Called when the user taps the Send button */
    public void calcTrilat(View view) {
        Trilat trilat = new Trilat();
        trilat.process();
        Log.d("crashCheck", "process ended");
        trilat.display();
        myView.scrollTo(0, 0);
    }

    /*Called when clear button pressed */
    public void clearInputs(View view) {
        clearHelp();
    }

    public void clearHelp() {
        // D1_L1
        EditText eD1L1 = findViewById(R.id.D1_L1);
        eD1L1.getText().clear();

        // D1_L2
        EditText eD1L2 = findViewById(R.id.D1_L2);
        eD1L2.getText().clear();

        // D1_L3
        EditText eD1L3 = findViewById(R.id.D1_L3);
        eD1L3.getText().clear();

        // D2_L1
        EditText eD2L1 = findViewById(R.id.D2_L1);
        eD2L1.getText().clear();

        // D2_L2
        EditText eD2L2 = findViewById(R.id.D2_L2);
        eD2L2.getText();

        // D2_L3
        EditText eD2L3 = findViewById(R.id.D2_L3);
        eD2L3.getText().clear();

        // D3_L1
        EditText eD3L1 = findViewById(R.id.D3_L1);
        eD3L1.getText().clear();

        // D3_L2
        EditText eD3L2 = findViewById(R.id.D3_L2);
        eD3L2.getText().clear();

        // D3_L3
        EditText eD3L3 = findViewById(R.id.D3_L3);
        eD3L3.getText().clear();

        // M1_D
        EditText eM1_D = findViewById(R.id.M1_D);
        eM1_D.setText("10");

        // M2_D
        EditText eM2_D = findViewById(R.id.M2_D);
        eM2_D.setText("10");

        // M1_B
        EditText eM1_B = findViewById(R.id.M1_B);
        eM1_B.setText("0");

        // M2_B
        EditText eM2_B = findViewById(R.id.M2_B);
        eM2_B.setText("90");
    }

    public class Trilat {
        // locations
        private double[][] loc;

        // current location num | start from 1
        private int curr_loc;

        // array of distance to landers from locations
        private double[][] l_dist;

        // array of lander locations
        private double[][] l_loc;

        // array of bearing directions
        private double[] l_bear;

        // variables
        double D1L1;
        double D1L2;
        double D1L3;
        double D2L1;
        double D2L2;
        double D2L3;
        double D3L1;
        double D3L2;
        double D3L3;
        double M1D;
        double M1B;
        double M2D;
        double M2B;


        public Trilat() {
            // start new initially
            reset();
        }

        // reset all coords
        private void reset() {
            // reset all variables
            loc = new double[3][2]; // list of locations | default instantiate 0
            loc[0] = new double[]{0, 0}; // for readability

            // for (int i = 0; i < loc.length; i++) {
            // 	System.out.print("(");
            // 	for (int j = 0; j < loc[0].length; j++) {
            // 		System.out.printf("%f, ", loc[i][j]);
            // 	}

            // 	System.out.println(")");
            // }

            curr_loc = 0; // current location numb | start from 0
            l_dist = new double[3][3]; // array of distance to landers from locations
            // set default value to -1
            for (double[] doubles : l_dist) {
                Arrays.fill(doubles, -1);
            }

            // array of location of landers
            l_loc = new double[3][2];
            // set default value to -1
            for (double[] doubles : l_loc) {
                Arrays.fill(doubles, -1);
            }

            // set default bearing result to impossible value 361
            l_bear = new double[3];
            Arrays.fill(l_bear, 361);

        }

        // polar vector of move
        private void polar_move(double[] polar, int curr_loc) {
            // if not sufficient move inputs, coord None
            if (polar == new double[]{-1, -1}) {
                loc[curr_loc + 1] = new double[]{-1, -1};
            }

            // if prev location None, new location None
            if (loc[curr_loc].equals(new double[]{-1, -1})) {
                loc[curr_loc + 1] = new double[]{-1, -1};
            }

            move(polar2cart(polar), curr_loc);
        }

        // cartesian vector of move
        private void move(double[] cart, int curr_loc) {
            if (curr_loc > 3) {
                throw new IllegalArgumentException("exceeded 3 points in trilateration");
            }

            double[] prev_cart = loc[curr_loc];
            double[] new_cart = new double[]{prev_cart[0] + cart[0], prev_cart[1] + cart[1]};
            System.out.printf("(%f, %f) moves (%f, %f) to (%f, %f)\n", prev_cart[0], prev_cart[1], cart[0], cart[1], new_cart[0], new_cart[1]);
            loc[curr_loc + 1] = new_cart;
            this.curr_loc++;
        }

        // find location of a point relative to 3 cartesian points using trilateration formula
        // formula from John in https://math.stackexchange.com/questions/884807/find-x-location-using-3-known-x-y-location-using-trilateration
        private double[] locate(double[][] carts, double[] dist) {
            if (carts.length != 3) {
                throw new IllegalArgumentException("need 3 points for trilateration");
            }

            double a = -2 * carts[0][0] + 2 * carts[1][0];
            double b = -2 * carts[0][1] + 2 * carts[1][1];

            double c = dist[0] * dist[0] - dist[1] * dist[1] - carts[0][0] * carts[0][0] + carts[1][0] * carts[1][0] - carts[0][1] * carts[0][1] + carts[1][1] * carts[1][1];

            double d = -2 * carts[1][0] + 2 * carts[2][0];
            double e = -2 * carts[1][1] + 2 * carts[2][1];

            double f = dist[1] * dist[1] - dist[2] * dist[2] - carts[1][0] * carts[1][0] + carts[2][0] * carts[2][0] - carts[1][1] * carts[1][1] + carts[2][1] * carts[2][1];

            // catch /0 case if no move is made
            // returns last point
            if (b * d == a * e) {
                return carts[2];
            }

            return new double[]{(c * e - f * b) / (e * a - b * d), (c * d - a * f) / (b * d - a * e)};
        }

        // calculate polar bearing given 2 cartesian coords
        private double bearing(double[] cart1, double[] cart2) {
            System.out.printf("(%f, %f), (%f, %f)\n", cart1[0], cart1[1], cart2[0], cart2[1]);
//            Log.d("crashCheck", String.format(Locale.US, "bearing recorded angle: %.0f", new double[]{cart2[0] - cart1[0], cart2[1] - cart1[1]}));
            return cart2polar(new double[]{cart2[0] - cart1[0], cart2[1] - cart1[1]})[1];
        }

        public void process() {
//            manualInput(); // input values manually
            // autoInput(); // use predefined values for debugging
            Log.d("crashCheck", "process called");
            setValues();
            Log.d("crashCheck", "values set");

            // check if all coords valid
            if (l_loc[2].equals(new double[]{-1, -1})) {
                System.out.println("Not enough points");
                return;
            }

            Log.d("crashCheck", "sub coords checked");

            // iterate through landers
            for (int i = 0; i < 3; i++) {
                boolean skip = false;
                double[] dist = new double[3];

                for (int j = 0; j < l_dist.length; j++) {
                    if (l_dist[j][i] == -1) {
                        skip = true;
                        break;
                    }

                    dist[j] = l_dist[j][i];
                }

                // if a lander not have inputs from 3 points
                if (skip) {
                    continue;
                }

                l_loc[i] = locate(loc, dist);
            }

            Log.d("crashCheck", "landers iterated");
//
//            System.out.println("Sub Locations");
//            for (int i = 0; i < loc.length; i++) {
//                System.out.print("(");
//                for (int j = 0; j < loc[0].length; j++) {
//                    System.out.printf("%f, ", loc[i][j]);
//                }
//
//                System.out.println(")");
//            }

            for (int i = 0; i < l_loc.length; i++) {
                if (l_loc[i][0] == -1) {
                    continue;
                }

                l_bear[i] = polar2bearing(bearing(loc[2], l_loc[i]));
                System.out.printf("Bearing to Lander %d: %f\n", i + 1, l_bear[i]);
            }

            Log.d("crashCheck", String.format(Locale.US, "bearing recorded angle: %.0f", l_bear[0]));
        }

        public void display() {
            TextView tv1 = findViewById(R.id.result_1);
            if (l_bear[0] != 361) {
                tv1.setText(String.format(Locale.US, "Bearing to Skaff: %.0f°", l_bear[0]));
            } else {
                tv1.setText("Bearing to Skaff: ");
            }

            TextView tv2 = findViewById(R.id.result_2);
            if (l_bear[1] != 361){
                tv2.setText(String.format(Locale.US, "Bearing to Flere: %.0f°", l_bear[1]));
            } else {
                tv2.setText("Bearing to Flere: ");
            }

            TextView tv3 = findViewById(R.id.result_3);
            if (l_bear[2] != 361){
                tv3.setText(String.format(Locale.US, "Bearing to Closp: %.0f°", l_bear[2]));
            } else {
                tv3.setText("Bearing to Closp: ");
            }
        }

        private void setValues() {
            // D1_L1
            EditText eD1L1 = findViewById(R.id.D1_L1);
            String tD1L1 = eD1L1.getText().toString();
            if (tD1L1.length() == 0){
                D1L1 = -1;
            } else {
                D1L1 = Double.parseDouble(tD1L1);
            }

            // D1_L2
            EditText eD1L2 = findViewById(R.id.D1_L2);
            String tD1L2 = eD1L2.getText().toString();
            if (tD1L2.length() == 0){
                D1L2 = -1;
            } else {
                D1L2 = Double.parseDouble(tD1L2);
            }

            // D1_L3
            EditText eD1L3 = findViewById(R.id.D1_L3);
            String tD1L3 = eD1L3.getText().toString();
            if (tD1L3.length() == 0){
                D1L3 = -1;
            } else {
                D1L3 = Double.parseDouble(tD1L3);
            }

            // D2_L1
            EditText eD2L1 = findViewById(R.id.D2_L1);
            String tD2L1 = eD2L1.getText().toString();
            if (tD2L1.length() == 0){
                D2L1 = -1;
            } else {
                D2L1 = Double.parseDouble(tD2L1);
            }

            // D2_L2
            EditText eD2L2 = findViewById(R.id.D2_L2);
            String tD2L2 = eD2L2.getText().toString();
            if (tD2L2.length() == 0){
                D2L2 = -1;
            } else {
                D2L2 = Double.parseDouble(tD2L2);
            }

            // D2_L3
            EditText eD2L3 = findViewById(R.id.D2_L3);
            String tD2L3 = eD2L3.getText().toString();
            if (tD2L3.length() == 0){
                D2L3 = -1;
            } else {
                D2L3 = Double.parseDouble(tD2L3);
            }

            // D3_L1
            EditText eD3L1 = findViewById(R.id.D3_L1);
            String tD3L1 = eD3L1.getText().toString();
            if (tD3L1.length() == 0){
                D3L1 = -1;
            } else {
                D3L1 = Double.parseDouble(tD3L1);
            }

            // D3_L2
            EditText eD3L2 = findViewById(R.id.D3_L2);
            String tD3L2 = eD3L2.getText().toString();
            if (tD3L2.length() == 0){
                D3L2 = -1;
            } else {
                D3L2 = Double.parseDouble(tD3L2);
            }

            // D3_L3
            EditText eD3L3 = findViewById(R.id.D3_L3);
            String tD3L3 = eD3L3.getText().toString();
            if (tD3L3.length() == 0){
                D3L3 = -1;
            } else {
                D3L3 = Double.parseDouble(tD3L3);
            }

            // M1_D
            EditText eM1_D = findViewById(R.id.M1_D);
            String tM1_D = eM1_D.getText().toString();
            if (tM1_D.length() == 0){
                M1D = -1;
            } else {
                M1D = Double.parseDouble(tM1_D);
            }

            // M2_D
            EditText eM2_D = findViewById(R.id.M2_D);
            String tM2_D = eM2_D.getText().toString();
            if (tM2_D.length() == 0){
                M2D = -1;
            } else {
                M2D = Double.parseDouble(tM2_D);
            }

            // M1_B
            EditText eM1_B = findViewById(R.id.M1_B);
            String tM1_B = eM1_B.getText().toString();
            if (tM1_B.length() == 0){
                M1B = 361;
            } else {
                M1B = Double.parseDouble(tM1_B);
            }

            // M2_B
            EditText eM2_B = findViewById(R.id.M2_B);
            String tM2_B = eM2_B.getText().toString();
            if (tM2_B.length() == 0){
                M2B = 361;
            } else {
                M2B = Double.parseDouble(tM2_B);
            }


            l_dist = new double[][]{new double[]{D1L1, D1L2, D1L3}, new double[]{D2L1, D2L2, D2L3}, new double[]{D3L1, D3L2, D3L3}};
            polar_move(new double[]{M1D, bearing2polar(M1B)}, 0);
            polar_move(new double[]{M2D, bearing2polar(M2B)}, 1);
        }

        // HELPER FUNCTIONS
        // helper function find distance between 2 cartesian points
        private double cartsDistance (double[] cart1, double[] cart2) {
            double xdist = Math.abs(cart2[0] - cart1[0]);
            double ydist = Math.abs(cart2[1] - cart2[1]);
            return Math.sqrt(xdist * xdist + ydist * ydist);
        }

        // helper function convert polar coords to cartesian coords relative to (0, 0)
        // degrees
        private double[] polar2cart (double[] polar) {
            if (polar.length != 2) {
                throw new IllegalArgumentException("polar input not valid, not valid coord");
            }

            return new double[]{polar[0] * Math.cos(Math.toRadians(polar[1])) , polar[0] * Math.sin(Math.toRadians(polar[1]))};
        }

        // helper function convert cartesian coords to polar coord relative to (0, 0)
        // degrees
        private double[] cart2polar (double[] cart) {
            // check for proper input
            if (cart.length != 2) {
                throw new IllegalArgumentException("cartesian input not valid, not valid coord");
            }

            // catch /0 case when vertical
            if (cart[0] == 0) {
                return new double[]{cart[1], 90};
            }

            double[] result = new double[]{Math.sqrt(cart[0] * cart[0] + cart[1] * cart[1]), Math.toDegrees(Math.atan(cart[1] / cart[0]))};
            if (cart[0] < 0) {
                result[1] += 180;
            }
            return result;
        }

        private double bearing2polar (double bearingDeg) {
            return -bearingDeg + 90;
        }

        private double polar2bearing (double polarDeg) {
            double result = -polarDeg + 90;

            if (result < 0) {
                result += 360;
            }

            return result;
        }
    }
}

