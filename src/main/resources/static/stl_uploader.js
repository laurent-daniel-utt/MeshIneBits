window.uploadFile = function() {
    const input = document.getElementById('stlFileInput');

    if (input.files.length > 0) {
        const file = input.files[0];

        const formData = new FormData();
        formData.append('stlFile', file);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/model-viewer/upload-stl', true);
        xhr.onload = function () {
            if (xhr.status === 200) {
                console.log('File uploaded successfully');
                window.location.href = "/model-viewer?name=" + xhr.response.toString();
            } else {
                console.error('File upload failed');
            }
        };
        xhr.send(formData);
    } else {
        console.error('No file selected');
    }
}

window.sliceModel = function() {
    fetch("/model-viewer/slice", {
        method : 'POST'
    }).then (response => {
        console.log(response.toString())
    })
}