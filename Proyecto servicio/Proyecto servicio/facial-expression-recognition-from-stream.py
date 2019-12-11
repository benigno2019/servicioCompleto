import os.path
import json
import numpy as np
import cv2
from keras.preprocessing import image
from flask import Flask,request,Response
import uuid
from scipy.spatial import distance as dist
from imutils.video import FileVideoStream
from imutils.video import VideoStream
from imutils import face_utils
import numpy as np
import argparse
import imutils
import time
import dlib
from multiprocessing import Process, Value
import threading
#-----------------------------
#opencv initialization

face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
cap = cv2.VideoCapture(0)
#-----------------------------
#face expression recognizer initialization
from keras.models import model_from_json
model = model_from_json(open("facial_expression_model_structure.json", "r").read())
model.load_weights('facial_expression_model_weights.h5') #load weights

#-----------------------------

emotions = ('enojado', 'adisgusto', 'asustado', 'feliz', 'triste', 'asombrado', 'neutral')

def faceDetect(img):
	gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        
        
        
	faces = face_cascade.detectMultiScale(gray, 1.3, 5)

	max_index = 7
	emotion = 'null'

	for (x,y,w,h) in faces:
		cv2.rectangle(img,(x,y),(x+w,y+h),(255,0,0),2) #dibuja el rectangulo de en la imagen principal
		
		detected_face = img[int(y):int(y+h), int(x):int(x+w)] #crop detected face
		detected_face = cv2.cvtColor(detected_face, cv2.COLOR_BGR2GRAY) #tranforma a escala de grises
		detected_face = cv2.resize(detected_face, (48, 48)) #ajusta la imagen a 48*48
		
		img_pixels = image.img_to_array(detected_face)
		img_pixels = np.expand_dims(img_pixels, axis = 0)
		
		img_pixels /= 255 #pixels are in scale of [0, 255]. normalize all pixels in scale of [0, 1]
		
		predictions = model.predict(img_pixels) #obtiene las expreciones de entre 7 predicciones
		
		#busca con el idice mas alto 0: angry, 1:disgust, 2:fear, 3:happy, 4:sad, 5:surprise, 6:neutral
		max_index = np.argmax(predictions[0])
		
		emotion = emotions[max_index]

		#write emotion text above rectangle
		cv2.putText(img, emotion, (int(x), int(y)), cv2.FONT_HERSHEY_SIMPLEX, 1, (255,255,255), 2)
		
		#-------------------------
        
	path_file=('static/%s.jpg' %uuid.uuid4().hex)
	cv2.imwrite(path_file,img)
	path_file=str(emotion)
	print('procesada')
	return json.dumps(path_file) #retorna el nombre de la imagen

#api
app = Flask(__name__)

#rutas http post
@app.route('/api/upload',methods=['POST'])
def upload():
    img = cv2.imdecode(np.fromstring(request.files['image'].read(),np.uint8),cv2.IMREAD_UNCHANGED)
    """path_file_global = ('%s.jpg' %uuid.uuid4().hex)
    cv2.imwrite(path_file_global,img)
    print(path_file_global)"""
    img_processed = faceDetect(img)
    return Response(response=img_processed,status=200,mimetype="application/json")

app.run(host="0.0.0.0",port=5000,threaded=False)
