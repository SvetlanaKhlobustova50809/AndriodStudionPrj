from flask import Flask, request, jsonify
from clarifai_grpc.channel.clarifai_channel import ClarifaiChannel
from clarifai_grpc.grpc.api import resources_pb2, service_pb2, service_pb2_grpc
from clarifai_grpc.grpc.api.status import status_code_pb2

app = Flask(__name__)

PAT = '23cc55fadbfe4aab8c3690cb8f357952'
USER_ID = 'clarifai'
APP_ID = 'main'
MODEL_ID = 'food-item-recognition'
MODEL_VERSION_ID = '1d5fd481e0cf4826aa72ec3ff049e044'

channel = ClarifaiChannel.get_grpc_channel()
stub = service_pb2_grpc.V2Stub(channel)

metadata = (('authorization', 'Key ' + PAT),)

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    image_url = data.get('image_url')

    if not image_url:
        return jsonify({'error': 'Image URL is required'}), 400

    userDataObject = resources_pb2.UserAppIDSet(user_id=USER_ID, app_id=APP_ID)

    post_model_outputs_response = stub.PostModelOutputs(
        service_pb2.PostModelOutputsRequest(
            user_app_id=userDataObject,
            model_id=MODEL_ID,
            version_id=MODEL_VERSION_ID,
            inputs=[
                resources_pb2.Input(
                    data=resources_pb2.Data(
                        image=resources_pb2.Image(
                            url=image_url
                        )
                    )
                )
            ]
        ),
        metadata=metadata
    )

    if post_model_outputs_response.status.code != status_code_pb2.SUCCESS:
        return jsonify({'error': post_model_outputs_response.status.description}), 500

    output = post_model_outputs_response.outputs[0]
#     concepts = [
#         concept.name for concept in output.data.concepts if concept.value >= 0.95
#     ]

    concept = output.data.concepts[0].name
    return jsonify({'predicted_concepts': concept})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)