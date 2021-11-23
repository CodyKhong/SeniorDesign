idf_component_register(SRCS "test_me_out_arduino.cpp"
                    INCLUDE_DIRS "." "thermistor" "components\arduino"
                    REQUIRES thermistor
                             components\arduino)