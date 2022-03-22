const functions = require('firebase-functions');

const admin = require('firebase-admin');
let hospital;
let topic;
let messageCondition;
admin.initializeApp();

exports.leaderboard = functions.region('europe-west1').https.onRequest((request, response) => {
  const users = [];
  admin.database().ref('/Users/')
      .once('value')
      .then((results) => {
        results.forEach((snapshot) => {
          users.push(snapshot.val());
        });
        users.sort((a, b) => (a.xp < b.xp) ? 1 : -1);
        response.send(users);
      });
});

exports.alertAdded = functions.region('europe-west1').database.ref('/Alerts/{alert_id}')
    .onCreate((snapshot, context) => {
      const alertData=snapshot.val();
      admin.database().ref('/Hospitals/'+alertData.owner)
          .once('value', (snap) => {
            console.log(snap.val());
            hospital=snap.val();
            topic=alertData.bloodType.replace('+', 'pos').replace('-', 'neg');
            console.log(topic);
            switch (topic) {
              case 'Apos':
                messageCondition='\'Apos\' in topics || \'Aneg\' in topics || \'Oneg\' in topics || \'Opos\' in topics';
                break;
              case 'Opos':
                messageCondition='\'Oneg\' in topics || \'Opos\' in topics';
                break;
              case 'Bpos':
                messageCondition='\'Bpos\' in topics || \'Bneg\' in topics || \'Oneg\' in topics || \'Opos\' in topics';
                break;
              case 'ABpos':
                // due to restriction of 5 topics we send to the top 5 most common.(according to redcrossblood.org).
                messageCondition='\'Apos\' in topics || \'ABpos\' in topics || \'Opos\' in topics || \'Bpos\' in topics || \'Oneg\' in topics';
                break;
              case 'Aneg':
                messageCondition='\'Aneg\' in topics || \'Oneg\' in topics';
                break;
              case 'Oneg':
                messageCondition='\'Oneg\' in topics';
                break;
              case 'Bneg':
                messageCondition='\'Bneg\' in topics || \'Oneg\' in topics';
                break;
              case 'ABneg':
                messageCondition='\'ABneg\' in topics || \'Aneg\' in topics || \'Oneg\' in topics || \'Bneg\' in topics';
                break;
            }
            console.log(messageCondition + ' message condition');
            const message = {
              data: {
                alert: JSON.stringify(alertData),
                hospital: JSON.stringify(hospital),
              },
              condition: messageCondition,
            };
            console.log(message);
            admin.messaging().send(message)
                .then((response)=>{
                  console.log('Successfully sent message:', response);
                }).catch((error) => {
                  console.log('Error sending message:', error);
                });
          }, (errorObject) => {
            console.log('The read failed: ' + errorObject.name);
          });
    });
let date;
let eventTime;
let currentTime;
// check all Events at 22:30 and deletes expired events
exports.scheduledEventDateCheck = functions.region('europe-west1').pubsub.schedule('30 22 * * *')
    .timeZone('Europe/Athens')
    .onRun((context) => {
      const currentDate = new Date();
      currentTime = currentDate.getTime();
      console.log('Server timestamp:'+ currentTime);
      admin.database().ref('/Events')
          .once('value')
          .then((results) => {
            results.forEach((snapshot) => {
              date = snapshot.val().date;
              eventTime = (new Date(date+' 20:00:00')).getTime(); // 23:55 PM - GMT
              if (eventTime<currentTime) {
                console.log(currentTime);
                const delRef = admin.database().ref('/Events/' + snapshot.key);
                delRef.remove()
                    .then(function() {
                      console.log('good');
                    })
                    .catch(function(error) {
                      console.log('bad');
                    });
              } else {
                console.log(eventTime);
              }
            });
          });
      return null;
    });
// Deletes all alerts at 23:59
exports.deleteAlerts = functions.region('europe-west1').pubsub.schedule('59 23 * * *')
    .timeZone('Europe/Athens')
    .onRun((context) => {
      const delRef = admin.database().ref('/Alerts/');
      delRef.remove()
          .then(function() {
            console.log('Alerts deleted');
          })
          .catch(function(error) {
            console.log('Alerts not deleted :(');
          });
    });

