import * as THREE from 'three';
import { STLLoader } from 'STLLoader';
import {OrbitControls} from 'OrbitControls'



// Initialize a scene, camera, and renderer
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 100000);
const renderer = new THREE.WebGLRenderer();
renderer.setSize(window.innerWidth, window.innerHeight);
document.getElementById('stl-container').appendChild(renderer.domElement);
let arrowPos = new THREE.Vector3( 0,0,0 );
scene.add( new THREE.ArrowHelper( new THREE.Vector3( 10,0,0 ), arrowPos, 60, 0x7F2020, 20, 10 ) );
scene.add( new THREE.ArrowHelper( new THREE.Vector3( 0,10,0 ), arrowPos, 60, 0x207F20, 20, 10 ) );
scene.add( new THREE.ArrowHelper( new THREE.Vector3( 0,0,10 ), arrowPos, 60, 0x20207F, 20, 10 ) );

// Create a loader for STL files
const loader = new STLLoader();

const controls = new OrbitControls(camera, renderer.domElement)
controls.enableDamping = true
const queryString = window.location.search;
console.log(queryString);

const urlParams = new URLSearchParams(queryString);

const fileName = urlParams.get('name');

// Define the data to be sent as query parameters
const data = {
    x: 1.0,
    y: 2.0,
    z: 3.0,
    rot: 45.0
};

// Define the URL for the POST request
const url = '/post-pos';

// Define the request options
const requestOptions = {
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(data).toString(),
};

// Send the POST request
fetch(url, requestOptions)
    .then(response => {
        if (response.status === 201) {
            return response.text();
        } else {
            throw new Error('Failed to create post');
        }
    })
    .then(responseText => {
        console.log('Server response: ' + responseText);
    })
    .catch(error => {
        console.error('Error: ' + error);
    });

// Load the STL model
loader.load('/download-stl?name='+fileName, (geometry) => {
    // Create a mesh and add it to the scene
    const material = new THREE.MeshNormalMaterial(); // You can use different materials
    const mesh = new THREE.Mesh(geometry, material);
    mesh.isDraggable = true;
    scene.add(mesh);

    // Set the camera position
    camera.position.z = 1000    ;

    // Create an animation loop
    const animate = () => {
        requestAnimationFrame(animate);

        controls.update();
        // Rotate the model (optional)
        mesh.rotation.x += 0.001;
        mesh.rotation.y += 0.001;

        renderer.render(scene, camera);
    };

    // Start the animation loop
    animate();
});