package com.example.serviciofrontend.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitCliente {
    private static Retrofit retrofitCliente = null;
    public static Retrofit getClienr(){
        if(retrofitCliente==null){
            retrofitCliente = new Retrofit.Builder()
                    .baseUrl("http://086dc108.ngrok.io")    // 10.0.2.2 es local host en el emulador
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofitCliente;
    }
}
