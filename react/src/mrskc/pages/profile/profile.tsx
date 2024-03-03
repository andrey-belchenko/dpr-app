import React, { useState } from 'react';
import './profile.scss';
import Form from 'devextreme-react/form';

export default function Profile() {
  const [notes, setNotes] = useState(
    ''
  );
  const employee = {
    ID: 7,
    FirstName: 'Андрей',
    LastName: 'Бельченко',
    Prefix: null,
    Position: null,
    Picture: null,//'images/employees/06.png',
    BirthDate:null,
    HireDate: null,
    Notes: null,
    Address: null
  };

  return (
    <React.Fragment>
      <h2 className={'content-block'}>Profile</h2>

      <div className={'content-block dx-card responsive-paddings'}>
        <div className={'form-avatar'}>
          <img
            alt={''}
            src={`https://js.devexpress.com/Demos/WidgetsGallery/JSDemos/${
              employee.Picture
            }`}
          />
        </div>
        <span>{notes}</span>
      </div>

      <div className={'content-block dx-card responsive-paddings'}>
        <Form
          id={'form'}
          defaultFormData={employee}
          onFieldDataChanged={e => e.dataField === 'Notes' && setNotes(e.value)}
          labelLocation={'top'}
          colCountByScreen={colCountByScreen}
        />
      </div>
    </React.Fragment>
  );
}

const colCountByScreen = {
  xs: 1,
  sm: 2,
  md: 3,
  lg: 4
};
